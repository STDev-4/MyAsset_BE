package io.api.myasset.domain.mission.service;

import io.api.myasset.domain.mission.dto.CachedRecommendedMission;
import io.api.myasset.domain.mission.dto.MissionAcceptRequest;
import io.api.myasset.domain.mission.dto.MissionAcceptResponse;
import io.api.myasset.domain.mission.dto.MissionDetailResponse;
import io.api.myasset.domain.mission.dto.MissionStartResponse;
import io.api.myasset.domain.mission.dto.TodayMissionResponse;
import io.api.myasset.domain.mission.entity.Mission;
import io.api.myasset.domain.mission.enums.MissionStatus;
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
    public MissionAcceptResponse acceptMission(Long userId, MissionAcceptRequest request) {
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

        Mission savedMission = missionRepository.save(mission);

        List<CachedRecommendedMission> remaining = cachedMissions.stream()
                .filter(item -> !item.recommendationId().equals(request.recommendationId()))
                .toList();

        missionCacheService.saveRecommendedMissionCache(userId, today, remaining);

        return new MissionAcceptResponse(
                savedMission.getId(),
                savedMission.getTitle(),
                savedMission.getDescription(),
                savedMission.getIconType(),
                savedMission.getRewardPoint(),
                savedMission.getExpectedSavingAmount()
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
			mission.getExpectedSavingAmount(),
			mission.getStatus().name(),
			missionJsonProvider.toList(mission.getBehaviorInsightsJson()),
			missionJsonProvider.toList(mission.getStatisticalReasonsJson()));
	}

	@Transactional
	public MissionStartResponse startMission(Long userId, Long missionId) {
		Mission mission = missionRepository.findByIdAndUserId(missionId, userId)
			.orElseThrow(() -> new BusinessException(MissionError.MISSION_NOT_FOUND));

		if (mission.getStatus() != MissionStatus.READY) {
			throw new BusinessException(MissionError.INVALID_MISSION_STATUS);
		}

		mission.start();

		return new MissionStartResponse(
			mission.getId(),
			mission.getStatus().name(),
			mission.getStartedAt());
	}
}
