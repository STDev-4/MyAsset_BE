package io.api.myasset.domain.mission.dto;

public record MissionStatusCardResponse(
	int todayMissionStartedCount,
	int todayMissionTotalCount,
	int yesterdayProgressPercent,
	String autoEvaluateRemainingTime,
	int totalPoint,
	int pointToNextTier) {
}
