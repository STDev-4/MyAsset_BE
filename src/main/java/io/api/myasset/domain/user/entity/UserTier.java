package io.api.myasset.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserTier {

	SEED(1, "새싹 리그", 500),
	EXPLORER(2, "탐험가 리그", 1500),
	ANALYST(3, "분석가 리그", 3000),
	RESEARCHER(4, "연구원 리그", 5000),
	MASTER(5, "마스터 리그", null);

	private final int order; // 정렬용
	private final String label; // 화면 표시용 (DB 저장 금지)
	private final Integer nextTierRequiredPoint;

	// 다음 티어 반환 (마지막이면 null)
	public UserTier next() {
		UserTier[] values = UserTier.values();
		int ordinal = this.ordinal();
		return (ordinal < values.length - 1) ? values[ordinal + 1] : null;
	}
}
