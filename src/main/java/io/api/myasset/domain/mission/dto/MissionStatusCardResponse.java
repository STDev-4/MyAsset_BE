package io.api.myasset.domain.mission.dto;

public record MissionStatusCardResponse(
        int todayMissionCompletedCount,
        int todayMissionTotalCount,
        int yesterdayProgressPercent,
        String autoEvaluateRemainingTime,
        int totalPoint,
        int pointToNextTier
) {
}