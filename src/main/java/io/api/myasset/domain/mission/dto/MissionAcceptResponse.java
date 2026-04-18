package io.api.myasset.domain.mission.dto;

public record MissionAcceptResponse(
	Long missionId,
	String title,
	String description,
	String iconType,
	Integer rewardPoint,
	Integer expectedSavingAmount) {
}
