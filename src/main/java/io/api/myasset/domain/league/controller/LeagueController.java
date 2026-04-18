package io.api.myasset.domain.league.controller;

import io.api.myasset.domain.league.dto.response.LeagueSelectedRankingResponse;
import io.api.myasset.domain.user.entity.UserTier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.domain.league.dto.response.LeagueRankingResponse;
import io.api.myasset.domain.league.service.LeagueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/league")
public class LeagueController {

	private final LeagueService leagueService;

	// 리그 랭킹 조회 API
	@GetMapping("/ranking")
	public LeagueRankingResponse getLeagueRanking(
		Authentication authentication,
		@RequestParam(defaultValue = "50")
		int size) {
		Long userId = (Long)authentication.getPrincipal();
		return leagueService.getLeagueRanking(userId, size);
	}

	// 선택한 리그 랭킹 조회
	@GetMapping("/selected")
	public LeagueSelectedRankingResponse getLeagueSelectedRanking(
		@RequestParam
		UserTier tier,
		@RequestParam(defaultValue = "50")
		int size) {
		return leagueService.getLeagueSelectedRanking(tier, size);
	}
}
