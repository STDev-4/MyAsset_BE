package io.api.myasset.domain.approval.application.dto;

import java.util.List;

public record SpendResponse(
	String yearMonth, // 조회 년월 (2026-03)
	long totalAmount, // 이번 달 총 지출
	long previousTotalAmount, // 전월 총 지출
	List<CategorySpending> categories // 업종별 지출 내역 (금액 내림차순)
) {
	public record CategorySpending(
		String category, // 가맹점 업종 (예: 음식점, 카페, 편의점)
		long amount // 지출 금액
	) {
	}
}
