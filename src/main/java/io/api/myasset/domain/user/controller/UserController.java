package io.api.myasset.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.domain.user.dto.UserMeResponse;
import io.api.myasset.domain.user.service.UserService;
import io.api.myasset.global.auth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	/** 내 정보 조회 - 닉네임, 티어, 포인트, 코인, 활성 캐릭터 */
	@GetMapping("/me")
	public ResponseEntity<UserMeResponse> getMyInfo() {
		Long userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.ok(userService.getMyInfo(userId));
	}
}
