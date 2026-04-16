package io.api.myasset.domain.user.dto;

import io.api.myasset.domain.user.entity.User;

public record UserMeResponse(
	String nickname,
	String tier,
	int point,
	int coin,
	ActiveCharacterInfo activeCharacter) {
	public record ActiveCharacterInfo(
		Long id,
		String name,
		String imageUrl) {
	}

	public static UserMeResponse of(User user, ActiveCharacterInfo activeCharacter) {
		return new UserMeResponse(
			user.getNickname(),
			user.getTier().name(),
			user.getPoint(),
			user.getCoin(),
			activeCharacter);
	}
}
