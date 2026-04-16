package io.api.myasset.domain.league.controller;

import io.api.myasset.domain.league.dto.response.LeagueSelectedRankingResponse;
import io.api.myasset.domain.user.entity.UserTier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.domain.league.dto.response.LeagueRankingResponse;
import io.api.myasset.domain.league.service.LeagueService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LeagueController {

	private final LeagueService leagueService;

	// 리그 랭킹 조회 API
	@GetMapping("/api/league/ranking")
	public LeagueRankingResponse getLeagueRanking(
		@RequestHeader("userId")
		Long userId,
		@RequestParam(defaultValue = "10")
		int size) {

		return leagueService.getLeagueRanking(userId, size);
	}

	// 선택한 리그 랭킹 조회
	@GetMapping("/api/league/selected")
	public LeagueSelectedRankingResponse getLeagueSelectedRanking(
		@RequestParam
		UserTier tier,
		@RequestParam(defaultValue = "50")
		int size) {
		return leagueService.getLeagueSelectedRanking(tier, size);
	}
}
