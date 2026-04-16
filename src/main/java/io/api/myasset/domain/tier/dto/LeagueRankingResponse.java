package io.api.myasset.domain.tier.dto;

import java.util.List;

public record LeagueRankingResponse(
	String tier,
	List<RankingEntry> rankings) {
	public record RankingEntry(
		int rank,
		Long userId,
		String nickname,
		int point,
		String activeCharacterImageUrl) {
	}
}
