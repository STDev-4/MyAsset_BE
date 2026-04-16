package io.api.myasset.domain.character.dto;

import jakarta.validation.constraints.NotNull;

public record ActiveCharacterRequest(
	@NotNull
	Long characterId) {
}
