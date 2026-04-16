package io.api.myasset.domain.home.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.domain.home.dto.response.HomeRankingPercentileResponse;
import io.api.myasset.domain.home.dto.response.HomeStreakResponse;
import io.api.myasset.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

	private final HomeService homeService;

	// 절약력 상위 퍼센트 조회
	@GetMapping("/ranking-percentile")
	public HomeRankingPercentileResponse getRankingPercentile(
		@AuthenticationPrincipal
		Long userId) {
		return homeService.getRankingPercentile(userId);
	}

	// 연속 절약일수 조회
	@GetMapping("/streak")
	public HomeStreakResponse getStreak(
		@AuthenticationPrincipal
		Long userId) {
		return homeService.getStreak(userId);
	}
}
