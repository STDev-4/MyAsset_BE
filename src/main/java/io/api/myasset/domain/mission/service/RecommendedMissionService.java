package io.api.myasset.domain.mission.service;

import io.api.myasset.domain.analysisinsight.converter.JsonListConverter;
import io.api.myasset.domain.gpt.executor.GptExecutor;
import io.api.myasset.domain.gpt.prompt.DataPrompt;
import io.api.myasset.domain.gpt.prompt.PromptTemplate;
import io.api.myasset.domain.gpt.prompt.mission.RecommendedMissionDomainPrompt;
import io.api.myasset.domain.mission.dto.GptRecommendedMissionResponse;
import io.api.myasset.domain.mission.dto.RecommendedMissionResponse;
import io.api.myasset.domain.mission.entity.RecommendedMission;
import io.api.myasset.domain.mission.repository.RecommendedMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendedMissionService {

    private final RecommendedMissionRepository recommendedMissionRepository;
    private final JsonListConverter jsonListConverter;
    private final GptExecutor gptExecutor;
    private final RecommendedMissionDomainPrompt recommendedMissionDomainPrompt;
    private final MissionCacheService missionCacheService;

    @Transactional
    public List<RecommendedMissionResponse> getRecommendedMissions(Long userId) {
        LocalDate today = LocalDate.now();

        List<RecommendedMissionResponse> cached = missionCacheService.getRecommendedMissions(userId, today);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        List<RecommendedMission> savedMissions = recommendedMissionRepository.findTodayRecommendedMissions(userId, today);
        if (!savedMissions.isEmpty()) {
            List<RecommendedMissionResponse> responses = savedMissions.stream()
                    .map(mission -> new RecommendedMissionResponse(
                            mission.getId(),
                            mission.getTitle(),
                            mission.getDescription(),
                            mission.getIconType(),
                            mission.getRewardPoint(),
                            mission.getExpectedSavingAmount()
                    ))
                    .toList();

            missionCacheService.saveRecommendedMissions(userId, today, responses);
            return responses;
        }

        PromptTemplate dataPrompt = new DataPrompt(
                "사용자 소비 데이터",
                """
                - 최근 소비에서 배달, 카페, 간식 지출 비중이 높다.
                - 소액 결제가 반복되는 패턴이 있다.
                - 저녁 시간대 충동 소비 가능성이 높다.
                - 즉시 만족형 소비를 줄이는 미션이 필요하다.
                """
        );

        GptRecommendedMissionResponse gptResponse = gptExecutor.execute(
                recommendedMissionDomainPrompt,
                dataPrompt,
                700,
                GptRecommendedMissionResponse.class
        );

        List<RecommendedMission> missions = gptResponse.missions().stream()
                .map(item -> RecommendedMission.of(
                        userId,
                        item.title(),
                        item.description(),
                        item.iconType(),
                        item.rewardPoint(),
                        item.expectedSavingAmount(),
                        jsonListConverter.toJson(item.behaviorInsights()),
                        jsonListConverter.toJson(item.statisticalReasons()),
                        today
                ))
                .toList();

        List<RecommendedMission> saved = recommendedMissionRepository.saveAll(missions);

        List<RecommendedMissionResponse> responses = saved.stream()
                .map(mission -> new RecommendedMissionResponse(
                        mission.getId(),
                        mission.getTitle(),
                        mission.getDescription(),
                        mission.getIconType(),
                        mission.getRewardPoint(),
                        mission.getExpectedSavingAmount()
                ))
                .toList();

        missionCacheService.saveRecommendedMissions(userId, today, responses);
        return responses;
    }
}