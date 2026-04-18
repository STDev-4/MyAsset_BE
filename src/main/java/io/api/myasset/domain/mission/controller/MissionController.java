package io.api.myasset.domain.mission.controller;

import io.api.myasset.domain.mission.dto.*;
import io.api.myasset.domain.mission.service.MissionDashboardService;
import io.api.myasset.domain.mission.service.MissionService;
import io.api.myasset.global.auth.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/missions")
public class MissionController {

	private final MissionService missionService;
	private final MissionDashboardService missionDashboardService;

	@PostMapping("/accept")
	public ResponseEntity<MissionAcceptResponse> acceptMission(@Valid @RequestBody
	MissionAcceptRequest request) {
		Long userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.status(HttpStatus.CREATED).body(missionService.acceptMission(userId, request));
	}

	@GetMapping("/today")
	public ResponseEntity<List<TodayMissionResponse>> getTodayMissions() {
		Long userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.ok(missionService.getTodayMissions(userId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<MissionDetailResponse> getMissionDetail(@PathVariable
	Long id) {
		Long userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.ok(missionService.getMissionDetail(userId, id));
	}

	@PostMapping("/{id}/start")
	public ResponseEntity<MissionStartResponse> startMission(@PathVariable
	Long id) {
		Long userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.ok(missionService.startMission(userId, id));
	}

	//미션탭 위쪽 대시보드
	@GetMapping("/status-card")
	public ResponseEntity<MissionStatusCardResponse> getMissionStatusCard() {
		Long userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.ok(missionDashboardService.getMissionStatusCard(userId));
	}
}
