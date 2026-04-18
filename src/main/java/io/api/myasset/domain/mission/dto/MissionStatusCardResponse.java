package io.api.myasset.domain.mission.dto;

public record MissionStatusCardResponse(
        int todayMissionInProgressCount,
        int todayMissionTotalCount,
        int todayProgressPercent,
        String autoEvaluateRemainingTime,
        int totalPoint,
        int pointToNextTier
) {
}