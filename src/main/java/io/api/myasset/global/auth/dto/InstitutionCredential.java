package io.api.myasset.global.auth.dto;

import io.api.myasset.domain.user.entity.InstitutionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InstitutionCredential(
	@NotNull(message = "기관 유형은 필수입니다.")
	InstitutionType institutionType,

	@NotBlank(message = "기관 로그인 아이디는 필수입니다.")
	String loginId,

	@NotBlank(message = "기관 로그인 비밀번호는 필수입니다.")
	String loginPassword
) {
}
