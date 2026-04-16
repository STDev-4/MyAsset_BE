package io.api.myasset.global.auth.dto;

import java.time.LocalDate;

import io.api.myasset.domain.user.entity.User;

public record SignupResponse(
	Long userId,
	String loginId,
	String email,
	String nickname,
	LocalDate birthDate) {
	public static SignupResponse from(User user) {
		return new SignupResponse(
			user.getId(),
			user.getLoginId(),
			user.getEmail(),
			user.getNickname(),
			user.getBirthDate());
	}
}
