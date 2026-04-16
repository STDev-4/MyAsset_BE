package io.api.myasset.domain.league.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

// 리그 랭킹 전체 응답 (내 정보 + 랭킹 리스트 + 남은 시간 포함)
@Getter
@Builder
public class LeagueRankingResponse {
    private MyLeagueInfoResponse myInfo;
    private String remainingTime;
    private Integer totalUserCount;
    private List<LeagueRankingUserResponse> rankings;
}