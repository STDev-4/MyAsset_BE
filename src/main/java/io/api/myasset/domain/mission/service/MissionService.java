package io.api.myasset.domain.mission.service;

import io.api.myasset.domain.analysisinsight.converter.JsonListConverter;
import io.api.myasset.domain.mission.dto.*;
import io.api.myasset.domain.mission.entity.Mission;
import io.api.myasset.domain.mission.entity.RecommendedMission;
import io.api.myasset.domain.mission.enums.MissionStatus;
import io.api.myasset.domain.mission.exception.MissionError;
import io.api.myasset.domain.mission.repository.MissionRepository;
import io.api.myasset.domain.mission.repository.RecommendedMissionRepository;
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
    private final RecommendedMissionRepository recommendedMissionRepository;
    private final JsonListConverter jsonListConverter;
    private final MissionCacheService missionCacheService;

    @Transactional
    public MissionAcceptResponse acceptMission(Long userId, MissionAcceptRequest request) {
        RecommendedMission recommendedMission = recommendedMissionRepository.findByIdAndUserId(request.missionId(), userId)
                .orElseThrow(() -> new BusinessException(MissionError.RECOMMENDED_MISSION_NOT_FOUND));

        if (recommendedMission.isAccepted()) {
            throw new BusinessException(MissionError.MISSION_ALREADY_ACCEPTED);
        }

        Mission mission = Mission.from(recommendedMission);
        recommendedMission.accept();
        Mission savedMission = missionRepository.save(mission);

        missionCacheService.evictRecommendedMissions(userId, LocalDate.now());

        return new MissionAcceptResponse(savedMission.getId());
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
                jsonListConverter.toList(mission.getBehaviorInsightsJson()),
                jsonListConverter.toList(mission.getStatisticalReasonsJson())
        );
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
                mission.getStartedAt()
        );
    }
}