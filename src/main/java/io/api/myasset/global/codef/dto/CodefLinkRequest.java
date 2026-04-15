package io.api.myasset.global.codef.dto;

import java.util.List;

import io.api.myasset.global.auth.dto.InstitutionCredential;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CodefLinkRequest(
	@NotEmpty(message = "연동할 기관 정보는 1개 이상이어야 합니다.")
	@Valid
	List<InstitutionCredential> institutions
) {
}
