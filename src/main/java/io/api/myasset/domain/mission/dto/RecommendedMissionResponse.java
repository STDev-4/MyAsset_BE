package io.api.myasset.domain.mission.dto;

public record RecommendedMissionResponse(
        String recommendationId,
        String title,
        String description,
        String iconType,
        Integer rewardPoint,
        Integer expectedSavingAmount
) {
}