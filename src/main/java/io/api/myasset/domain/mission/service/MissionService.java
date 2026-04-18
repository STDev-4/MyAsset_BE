package io.api.myasset.domain.mission.service;

import io.api.myasset.domain.mission.dto.*;
import io.api.myasset.domain.mission.entity.Mission;
import io.api.myasset.domain.mission.exception.MissionError;
import io.api.myasset.domain.mission.provider.MissionJsonProvider;
import io.api.myasset.domain.mission.repository.MissionRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final MissionJsonProvider missionJsonProvider;
    private final MissionCacheService missionCacheService;

    @Transactional
    public MissionStartResponse startMission(Long userId, MissionStartRequest request) {
        LocalDate today = LocalDate.now();

        List<CachedRecommendedMission> cachedMissions =
                missionCacheService.getRecommendedMissionCache(userId, today);

        if (cachedMissions == null || cachedMissions.isEmpty()) {
            throw new BusinessException(MissionError.RECOMMENDED_MISSION_NOT_FOUND);
        }

        if (missionRepository.existsAcceptedMission(userId, request.recommendationId())) {
            throw new BusinessException(MissionError.MISSION_ALREADY_ACCEPTED);
        }

        CachedRecommendedMission selected = cachedMissions.stream()
                .filter(item -> item.recommendationId().equals(request.recommendationId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(MissionError.RECOMMENDED_MISSION_NOT_FOUND));

        Mission mission = Mission.of(
                userId,
                selected.title(),
                selected.description(),
                selected.iconType(),
                selected.rewardPoint(),
                selected.expectedSavingAmount(),
                missionJsonProvider.toJson(selected.behaviorInsights()),
                missionJsonProvider.toJson(selected.statisticalReasons()),
                selected.recommendationId()
        );

        mission.start();

        Mission savedMission = missionRepository.save(mission);

        List<CachedRecommendedMission> remaining = cachedMissions.stream()
                .filter(item -> !item.recommendationId().equals(request.recommendationId()))
                .toList();

        missionCacheService.saveRecommendedMissionCache(userId, today, remaining);

        return new MissionStartResponse(
                savedMission.getId(),
                savedMission.getStatus().name(),
                savedMission.getStartedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<TodayMissionResponse> getTodayMissions(Long userId) {
        return missionRepository.findTodayMissions(userId, LocalDate.now()).stream()
                .map(mission -> new TodayMissionResponse(
                        mission.getId(),
                        mission.getTitle(),
                        mission.getIconType(),
                        mission.getStatus().name(),
                        mission.getRewardPoint(),
                        mission.getRewardPoint() / 2,
                        mission.getAutoEvaluateAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public MissionDetailResponse getMissionDetail(Long userId, Long missionId) {
        Mission mission = missionRepository.findByIdAndUserId(missionId, userId)
                .orElseThrow(() -> new BusinessException(MissionError.MISSION_NOT_FOUND));

        return new MissionDetailResponse(
                mission.getId(),
                mission.getTitle(),
                mission.getDescription(),
                mission.getIconType(),
                mission.getRewardPoint(),
                mission.getRewardPoint() / 2,
                mission.getExpectedSavingAmount(),
                mission.getStatus().name(),
                missionJsonProvider.toList(mission.getBehaviorInsightsJson()),
                missionJsonProvider.toList(mission.getStatisticalReasonsJson()));
    }
}