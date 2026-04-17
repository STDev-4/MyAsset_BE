package io.api.myasset.domain.mission.dto;

import java.util.List;

public record MissionDetailResponse(
        Long missionId,
        String title,
        String description,
        String iconType,
        Integer rewardPoint,
        Integer expectedSavingAmount,
        String status,
        List<String> behaviorInsights,
        List<String> statisticalReasons
) {
}