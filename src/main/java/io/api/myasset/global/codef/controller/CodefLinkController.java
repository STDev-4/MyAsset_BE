package io.api.myasset.global.codef.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.domain.approval.application.SyncStatus;
import io.api.myasset.domain.approval.application.SyncStatusService;
import io.api.myasset.domain.approval.application.dto.SyncStatusResponse;
import io.api.myasset.global.auth.util.SecurityUtil;
import io.api.myasset.global.codef.dto.CodefLinkRequest;
import io.api.myasset.global.codef.dto.CodefLinkResponse;
import io.api.myasset.global.codef.service.CodefLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/codef")
@RequiredArgsConstructor
public class CodefLinkController {

	private final CodefLinkService codefLinkService;
	private final SyncStatusService syncStatusService;

	@PostMapping("/link")
	public ResponseEntity<CodefLinkResponse> link(
		@Valid @RequestBody
		CodefLinkRequest request) {
		CodefLinkResponse response = codefLinkService.link(SecurityUtil.getCurrentUserId(), request);
		return ResponseEntity.ok(response);
	}

	/**
	 * 현재 유저의 CodefSyncJob 진행 상태 조회.
	 * <p>
	 * FE LoadingCompletePage 가 {@code /link} 호출 직후부터 3초 간격으로 polling 하며,
	 * {@link SyncStatus#COMPLETED} 수신 시 HomePage 로 전환하는 용도.
	 */
	@GetMapping("/sync/status")
	public ResponseEntity<SyncStatusResponse> getSyncStatus() {
		SyncStatus status = syncStatusService.get(SecurityUtil.getCurrentUserId());
		return ResponseEntity.ok(SyncStatusResponse.of(status));
	}
}
