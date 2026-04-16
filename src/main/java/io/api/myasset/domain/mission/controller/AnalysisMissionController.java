package io.api.myasset.domain.mission.controller;

import io.api.myasset.domain.mission.dto.RecommendedMissionResponse;
import io.api.myasset.domain.mission.service.RecommendedMissionService;
import io.api.myasset.global.auth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisMissionController {

    private final RecommendedMissionService recommendedMissionService;

    @GetMapping("/recommended-missions")
    public ResponseEntity<List<RecommendedMissionResponse>> getRecommendedMissions() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(recommendedMissionService.getRecommendedMissions(userId));
    }
}