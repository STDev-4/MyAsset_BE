package io.api.myasset.domain.mission.controller;

import io.api.myasset.domain.mission.dto.*;
import io.api.myasset.domain.mission.service.MissionDashboardService;
import io.api.myasset.domain.mission.service.MissionService;
import io.api.myasset.domain.mission.service.RecommendedMissionService;
import io.api.myasset.global.auth.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;
    private final MissionDashboardService missionDashboardService;
    private final RecommendedMissionService recommendedMissionService;

    @GetMapping("/recommended")
    public ResponseEntity<List<RecommendedMissionResponse>> getRecommendedMissions() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(recommendedMissionService.getRecommendedMissions(userId));
    }

    @PostMapping("/start")
    public ResponseEntity<MissionStartResponse> startMission(@Valid @RequestBody MissionStartRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(missionService.startMission(userId, request));
    }

    @GetMapping("/today")
    public ResponseEntity<List<TodayMissionResponse>> getTodayMissions() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(missionService.getTodayMissions(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MissionDetailResponse> getMissionDetail(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(missionService.getMissionDetail(userId, id));
    }

    @GetMapping("/status-card")
    public ResponseEntity<MissionStatusCardResponse> getMissionStatusCard() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(missionDashboardService.getMissionStatusCard(userId));
    }
}