package io.api.myasset.domain.approval.application;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.api.myasset.domain.approval.application.dto.SpendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 소비 집계 결과({@link SpendResponse}) 를 Redis 에 캐싱.
 * <p>
 * - Key 포맷: {@code v1:analysis:spending:{userId}:{yearMonth}} (yearMonth 는 {@code yyyy-MM})
 * - TTL: 48h ± 30m jitter (Batch 1회 실패해도 하루 이상 버티도록 여유)
 * - 버전 prefix(v1) 는 DTO 스키마 변경 시 별도 공간으로 이동할 수 있게 하기 위함
 * <p>
 * Redis 장애 시 조용히 DB 폴백으로 흘려보내도록 예외를 log 로만 기록한다 (fail-open).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingCacheService {

	private static final String KEY_PREFIX = "v1:analysis:spending";
	private static final Duration BASE_TTL = Duration.ofHours(48);
	private static final long JITTER_MILLIS = Duration.ofMinutes(30).toMillis();

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public Optional<SpendResponse> get(Long userId, String yearMonth) {
		String key = buildKey(userId, yearMonth);
		try {
			String json = redisTemplate.opsForValue().get(key);
			if (json == null) {
				return Optional.empty();
			}
			return Optional.of(objectMapper.readValue(json, SpendResponse.class));
		} catch (Exception e) {
			log.warn("[SpendingCache] 읽기 실패, DB 폴백 - key={}, reason={}", key, e.getMessage());
			return Optional.empty();
		}
	}

	public void put(Long userId, String yearMonth, SpendResponse value) {
		String key = buildKey(userId, yearMonth);
		try {
			String json = objectMapper.writeValueAsString(value);
			redisTemplate.opsForValue().set(key, json, ttlWithJitter());
			log.debug("[SpendingCache] 저장 완료 - key={}, categories={}", key, value.categories().size());
		} catch (Exception e) {
			log.warn("[SpendingCache] 쓰기 실패, 무시 - key={}, reason={}", key, e.getMessage());
		}
	}

	/**
	 * endDate(yyyyMMdd) 를 yearMonth(yyyy-MM) 로 변환하는 유틸.
	 * ApprovalService / Batch 가 공통으로 사용.
	 */
	public static String toYearMonth(String endDate) {
		return endDate.substring(0, 4) + "-" + endDate.substring(4, 6);
	}

	private String buildKey(Long userId, String yearMonth) {
		return "%s:%d:%s".formatted(KEY_PREFIX, userId, yearMonth);
	}

	/**
	 * 48h 기준 ±30분 난수 — Batch 실패 시 mini-avalanche 완화용.
	 */
	private Duration ttlWithJitter() {
		long delta = ThreadLocalRandom.current().nextLong(-JITTER_MILLIS, JITTER_MILLIS + 1);
		return BASE_TTL.plusMillis(delta);
	}
}
