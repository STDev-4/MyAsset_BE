package io.api.myasset.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserTier {

    SEED(1, "새싹 리그"),
    EXPLORER(2, "탐험가 리그"),
    ANALYST(3, "분석가 리그"),
    RESEARCHER(4, "연구원 리그"),
    MASTER(5, "마스터 리그");

    private final int order;
    private final String label;
}