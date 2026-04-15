package io.api.myasset.global.codef.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@PostMapping("/link")
	public ResponseEntity<CodefLinkResponse> link(@Valid @RequestBody CodefLinkRequest request) {
		CodefLinkResponse response = codefLinkService.link(SecurityUtil.getCurrentUserId(), request);
		return ResponseEntity.ok(response);
	}
}
