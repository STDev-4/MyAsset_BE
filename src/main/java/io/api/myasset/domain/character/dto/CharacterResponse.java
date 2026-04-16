package io.api.myasset.domain.character.dto;

import io.api.myasset.domain.character.entity.Character;
import io.api.myasset.domain.character.entity.UserCharacter;

public record CharacterResponse(
	Long id,
	String name,
	String imageUrl,
	int coinPrice,
	boolean owned,
	boolean active
) {
	public static CharacterResponse ofOwned(UserCharacter userCharacter) {
		Character character = userCharacter.getCharacter();
		return new CharacterResponse(
			character.getId(),
			character.getName(),
			character.getImageUrl(),
			character.getCoinPrice(),
			true,
			userCharacter.isActive()
		);
	}

	public static CharacterResponse ofUnowned(Character character) {
		return new CharacterResponse(
			character.getId(),
			character.getName(),
			character.getImageUrl(),
			character.getCoinPrice(),
			false,
			false
		);
	}
}
