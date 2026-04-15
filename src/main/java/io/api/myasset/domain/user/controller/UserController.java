package io.api.myasset.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.domain.user.dto.request.SignupRequest;
import io.api.myasset.domain.user.dto.response.SignupResponse;
import io.api.myasset.domain.user.service.UserSignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserSignupService userSignupService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/signup")
	public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
		return userSignupService.signup(request);
	}
}
