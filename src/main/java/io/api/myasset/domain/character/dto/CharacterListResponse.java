package io.api.myasset.domain.character.dto;

import java.util.List;

public record CharacterListResponse(
	List<CharacterResponse> characters,
	int coin) {
}
