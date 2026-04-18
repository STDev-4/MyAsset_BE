package io.api.myasset.domain.mission.dto;

import java.time.LocalDateTime;

public record TodayMissionResponse(
	Long missionId,
	String title,
	String iconType,
	String status,
	Integer rewardPoint,
	LocalDateTime autoEvaluateAt) {
}
