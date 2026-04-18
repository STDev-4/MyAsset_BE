package io.api.myasset.domain.mission.service;

import io.api.myasset.domain.approval.application.ApprovalService;
import io.api.myasset.domain.approval.application.dto.SpendingTopResponse;
import io.api.myasset.domain.gpt.executor.GptExecutor;
import io.api.myasset.domain.gpt.prompt.DataPrompt;
import io.api.myasset.domain.gpt.prompt.PromptTemplate;
import io.api.myasset.domain.gpt.prompt.mission.RecommendedMissionDomainPrompt;
import io.api.myasset.domain.mission.dto.CachedRecommendedMission;
import io.api.myasset.domain.mission.dto.GptRecommendedMissionResponse;
import io.api.myasset.domain.mission.dto.RecommendedMissionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendedMissionService {

	private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final Duration GPT_LOCK_TTL = Duration.ofSeconds(60);

	private final GptExecutor gptExecutor;
	private final RecommendedMissionDomainPrompt recommendedMissionDomainPrompt;
	private final MissionCacheService missionCacheService;
	private final ApprovalService approvalService;
	private final RedisTemplate<String, String> redisTemplate;

	public List<RecommendedMissionResponse> getRecommendedMissions(Long userId) {
		LocalDate today = LocalDate.now();

		// 1단계: 캐시 hit
		List<RecommendedMissionResponse> cached = readCache(userId, today);
		if (cached != null) {
			return cached;
		}

		// 2단계: 분산 락 획득 시도
		String lockKey = "lock:gpt:recommended-missions:" + userId;
		Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", GPT_LOCK_TTL);
		if (!Boolean.TRUE.equals(acquired)) {
			log.info("[RecommendedMission] 동시 GPT 요청 감지, 빈 응답 반환 - userId={} (FE 가 재시도)", userId);
			return Collections.emptyList();
		}

		try {
			// 락 획득 후 race 방지 double-check
			cached = readCache(userId, today);
			if (cached != null) {
				return cached;
			}
			return generateAndSave(userId, today);
		} finally {
			redisTemplate.delete(lockKey);
		}
	}

	private List<RecommendedMissionResponse> readCache(Long userId, LocalDate today) {
		List<CachedRecommendedMission> cached = missionCacheService.getRecommendedMissionCache(userId, today);
		if (cached == null) {
			return null;
		}
        return cached.stream()
                .map(item -> new RecommendedMissionResponse(
                        item.recommendationId(),
                        item.title(),
                        item.description(),
                        item.iconType(),
                        item.rewardPoint(),
                        item.rewardPoint() / 2,
                        item.expectedSavingAmount()))
                .toList();
	}

	private List<RecommendedMissionResponse> generateAndSave(Long userId, LocalDate today) {
		String endDate = YearMonth.now()
			.minusMonths(1)
			.atEndOfMonth()
			.format(YYYYMMDD);

		SpendingTopResponse topSpending = approvalService.getTopSpending(userId, endDate, 3);

		String spendingData = buildSpendingDataPrompt(topSpending);

		PromptTemplate dataPrompt = new DataPrompt(
			"사용자 소비 데이터",
			spendingData);

		GptRecommendedMissionResponse gptResponse = gptExecutor.execute(
			recommendedMissionDomainPrompt,
			dataPrompt,
			700,
			GptRecommendedMissionResponse.class);

		List<CachedRecommendedMission> cacheItems = gptResponse.missions().stream()
			.map(item -> new CachedRecommendedMission(
				UUID.randomUUID().toString(),
				item.title(),
				item.description(),
				item.iconType(),
				item.rewardPoint(),
				item.expectedSavingAmount(),
				item.behaviorInsights(),
				item.statisticalReasons()))
			.toList();

		missionCacheService.saveRecommendedMissionCache(userId, today, cacheItems);

        return cacheItems.stream()
                .map(item -> new RecommendedMissionResponse(
                        item.recommendationId(),
                        item.title(),
                        item.description(),
                        item.iconType(),
                        item.rewardPoint(),
                        item.rewardPoint() / 2,
                        item.expectedSavingAmount()))
                .toList();
	}

	private String buildSpendingDataPrompt(SpendingTopResponse topSpending) {
		if (topSpending == null || topSpending.items() == null || topSpending.items().isEmpty()) {
			return """
				- 지난달 소비 데이터가 충분하지 않다.
				- 식비, 카페, 쇼핑, 교통 등 일상 소비에서 바로 줄일 수 있는 절약 미션을 추천한다.
				- 오늘 바로 실천 가능한 수준의 미션만 제안한다.
				""";
		}

		String top3Text = topSpending.items().stream()
			.map(item -> "- " + item.rank() + "위: " + item.category() + " (" + item.amount() + "원)")
			.reduce((a, b) -> a + "\n" + b)
			.orElse("- 소비 데이터 없음");

		return """
			- 사용자의 지난달 소비 상위 업종은 다음과 같다.
			%s
			- 반드시 위 상위 소비 업종을 우선 반영해서 미션을 생성한다.
			- 소비가 큰 업종일수록 더 직접적으로 줄일 수 있는 행동 미션을 제안한다.
			- 각 미션은 해당 소비 업종과 연결되어야 한다.
			- 오늘 바로 실천 가능한 수준의 미션만 제안한다.
			""".formatted(top3Text);
	}
}
