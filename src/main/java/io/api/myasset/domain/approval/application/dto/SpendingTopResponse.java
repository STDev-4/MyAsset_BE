package io.api.myasset.domain.approval.application.dto;

import java.util.List;

/**
 * 지난달 Top N 소비 업종 응답.
 * <p>
 * CODEF 의 {@code resMemberStoreType} (가맹점 업종) 단위로 집계한다.
 * 예: "음식점", "편의점", "카페" 등.
 */
public record SpendingTopResponse(
	String yearMonth,
	List<TopCategory> items) {
	public record TopCategory(
		int rank,
		String category,
		long amount) {
	}
}
