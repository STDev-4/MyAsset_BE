package io.api.myasset.domain.league.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

// 선택한 리그의 랭킹 전체 응답
@Getter
@Builder
public class LeagueSelectedRankingResponse {
	private String remainingTime;
	private Integer totalUserCount;
	private List<LeagueRankingUserResponse> rankings;
}
