package io.api.myasset.global.codef.dto;

import java.util.List;

import io.api.myasset.domain.user.entity.InstitutionType;

public record CodefLinkResponse(
	List<InstitutionType> linked,
	List<InstitutionType> failed
) {
}
