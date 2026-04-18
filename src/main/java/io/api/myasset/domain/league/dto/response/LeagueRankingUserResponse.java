package io.api.myasset.domain.league.dto.response;

import lombok.Builder;
import lombok.Getter;

// 리그 랭킹 리스트에 포함되는 사용자 정보 응답
@Getter
@Builder
public class LeagueRankingUserResponse {
	private Integer rank;
	private String nickname;
	private String profileImageUrl;
	private Integer point;
	private String lastLoginAt;
	private Boolean isActive;
}
