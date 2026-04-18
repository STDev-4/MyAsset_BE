package io.api.myasset.domain.mission.dto;

import java.io.Serializable;
import java.util.List;

public record CachedRecommendedMission(
	String recommendationId,
	String title,
	String description,
	String iconType,
	Integer rewardPoint,
	Integer expectedSavingAmount,
	List<String> behaviorInsights,
	List<String> statisticalReasons) implements Serializable {
}
