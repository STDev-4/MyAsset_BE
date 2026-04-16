package io.api.myasset.global.auth.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record SignupRequest(
	@NotBlank(message = "아이디는 필수입니다.") @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하여야 합니다.")
	String loginId,

	@NotBlank(message = "비밀번호는 필수입니다.") @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
	String password,

	@NotBlank(message = "이메일은 필수입니다.") @Email(message = "올바른 이메일 형식이 아닙니다.")
	String email,

	@NotBlank(message = "닉네임은 필수입니다.") @Size(min = 2, max = 15, message = "닉네임은 2자 이상 15자 이하여야 합니다.")
	String nickname,

	@NotNull(message = "생년월일은 필수입니다.") @Past(message = "생년월일은 과거 날짜여야 합니다.")
	LocalDate birthDate) {
}
