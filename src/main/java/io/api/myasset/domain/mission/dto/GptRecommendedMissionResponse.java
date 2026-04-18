package io.api.myasset.domain.mission.dto;

import java.util.List;

public record GptRecommendedMissionResponse(
	List<MissionItem> missions) {
	public record MissionItem(
		String title,
		String description,
		String iconType,
		Integer rewardPoint,
		Integer expectedSavingAmount,
		List<String> behaviorInsights,
		List<String> statisticalReasons) {
	}
}
