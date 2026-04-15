package io.api.myasset.domain.user.exception;

import org.springframework.http.HttpStatus;

import io.api.myasset.global.exception.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserError implements ErrorCode {

	DUPLICATE_LOGIN_ID("이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT, "U_001"),
	DUPLICATE_EMAIL("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT, "U_002"),
	USER_NOT_FOUND("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND, "U_003"),
	INVALID_PASSWORD("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED, "U_004"),
	INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED, "U_005"),
	UNAUTHORIZED("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED, "U_006");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
