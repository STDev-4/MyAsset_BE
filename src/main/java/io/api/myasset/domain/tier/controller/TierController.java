package io.api.myasset.domain.tier.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.domain.tier.dto.LeagueRankingResponse;
import io.api.myasset.domain.tier.dto.TierMeResponse;
import io.api.myasset.domain.tier.service.TierService;
import io.api.myasset.global.auth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tier")
@RequiredArgsConstructor
public class TierController {

	private final TierService tierService;

	/** 내 티어/포인트 조회 - 현재 티어 + P + 다음 승급까지 */
	@GetMapping("/me")
	public ResponseEntity<TierMeResponse> getMyTier() {
		Long userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.ok(tierService.getMyTier(userId));
	}

	/** 티어별 리그 랭킹 조회 */
	@GetMapping("/leagues/{tierId}")
	public ResponseEntity<LeagueRankingResponse> getLeagueRanking(@PathVariable
	String tierId) {
		return ResponseEntity.ok(tierService.getLeagueRanking(tierId));
	}
}
