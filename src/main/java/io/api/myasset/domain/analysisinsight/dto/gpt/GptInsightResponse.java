package io.api.myasset.domain.analysisinsight.dto.gpt;

import io.api.myasset.domain.analysisinsight.enums.InsightColorType;

import java.util.List;

public record GptInsightResponse(
	List<InsightItem> insights) {
	public record InsightItem(
		String title,
		String description,
		InsightColorType colorType,
		List<String> actionTips) {
	}
}
