package io.api.myasset.domain.character.exception;

import org.springframework.http.HttpStatus;

import io.api.myasset.global.exception.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CharacterError implements ErrorCode {

	CHARACTER_NOT_FOUND("존재하지 않는 캐릭터입니다.", HttpStatus.NOT_FOUND, "C_001"),
	ALREADY_UNLOCKED("이미 보유한 캐릭터입니다.", HttpStatus.CONFLICT, "C_002"),
	INSUFFICIENT_COIN("코인이 부족합니다.", HttpStatus.BAD_REQUEST, "C_003"),
	CHARACTER_NOT_OWNED("보유하지 않은 캐릭터입니다.", HttpStatus.FORBIDDEN, "C_004");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
