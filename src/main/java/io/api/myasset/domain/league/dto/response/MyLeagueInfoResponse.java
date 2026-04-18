package io.api.myasset.domain.league.dto.response;

import lombok.Builder;
import lombok.Getter;

// 로그인한 사용자의 리그 내 정보 응답
@Getter
@Builder
public class MyLeagueInfoResponse {
	private String nickname;
	private String profileImageUrl;
	private Integer point;
	private String tier;
	private Integer rank;
	private String lastLoginAt;
	private Boolean isActive;
}
