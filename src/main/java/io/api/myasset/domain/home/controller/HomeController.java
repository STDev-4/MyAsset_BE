package io.api.myasset.domain.home.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.domain.home.dto.response.HomeRankingPercentileResponse;
import io.api.myasset.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

	private final HomeService homeService;
    // 절약력 상위 퍼센트 조회
    @GetMapping("/percentile")
    public HomeRankingPercentileResponse getRankingPercentile(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return homeService.getRankingPercentile(userId);
    }

}
