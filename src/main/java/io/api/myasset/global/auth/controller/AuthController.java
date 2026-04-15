package io.api.myasset.global.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.global.auth.cookie.CookieProvider;
import io.api.myasset.global.auth.dto.LoginRequest;
import io.api.myasset.global.auth.dto.SignupRequest;
import io.api.myasset.global.auth.dto.SignupResponse;
import io.api.myasset.global.auth.service.AuthService;
import io.api.myasset.global.auth.service.AuthService.SignupResult;
import io.api.myasset.global.auth.service.AuthService.TokenPair;
import io.api.myasset.global.auth.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final CookieProvider cookieProvider;

	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
		SignupResult result = authService.signup(request);

		return ResponseEntity.status(HttpStatus.CREATED)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + result.tokens().accessToken())
			.header(HttpHeaders.SET_COOKIE, cookieProvider.createRefreshTokenCookie(result.tokens().refreshToken()).toString())
			.body(result.userInfo());
	}

	@PostMapping("/login")
	public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
		TokenPair tokenPair = authService.login(request);

		return ResponseEntity.ok()
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.accessToken())
			.header(HttpHeaders.SET_COOKIE, cookieProvider.createRefreshTokenCookie(tokenPair.refreshToken()).toString())
			.build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<Void> refresh(
		@CookieValue(name = "refresh_token", required = false) String refreshToken
	) {
		TokenPair tokenPair = authService.refresh(refreshToken);

		return ResponseEntity.ok()
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.accessToken())
			.header(HttpHeaders.SET_COOKIE, cookieProvider.createRefreshTokenCookie(tokenPair.refreshToken()).toString())
			.build();
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout() {
		authService.logout(SecurityUtil.getCurrentUserId());

		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			.header(HttpHeaders.SET_COOKIE, cookieProvider.expireRefreshTokenCookie().toString())
			.build();
	}
}
