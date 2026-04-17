package io.api.myasset.domain.analysisinsight.dto;

import io.api.myasset.domain.analysisinsight.enums.InsightColorType;

import java.util.List;

public record AnalysisInsightItemResponse(
        Long insightId,
        String title,
        String description,
        InsightColorType colorType,
        List<String> actionTips
) {
}