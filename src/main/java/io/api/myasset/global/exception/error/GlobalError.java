package io.api.myasset.global.exception.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GlobalError implements ErrorCode {

	// 400
	REQUEST_HEADER_EMPTY("요청 헤더가 비어있습니다.", HttpStatus.BAD_REQUEST, "G_001"),
	NOT_VALID_EXCEPTION("유효하지 않은 요청입니다.", HttpStatus.BAD_REQUEST, "G_002"),
	INVALID_REQUEST_BODY("요청 본문이 올바르지 않습니다.", HttpStatus.BAD_REQUEST, "G_003"),
	INVALID_VALUE("값이 비어있거나 유효하지 않습니다.", HttpStatus.BAD_REQUEST, "G_004"),
	MISSING_REQUEST_PARAMETER("필수 요청 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST, "G_005"),

	// 404
	NOT_FOUND_URL("존재하지 않는 URL 입니다.", HttpStatus.NOT_FOUND, "G_006"),

	// 405
	METHOD_NOT_ALLOWED("지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED, "G_007"),

	// 406
	HTTP_MEDIA_TYPE_NOT_ACCEPTABLE("요청한 미디어 타입을 제공할 수 없습니다.", HttpStatus.NOT_ACCEPTABLE, "G_008"),

	// 401
	INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED, "G_010"),
	EXPIRED_TOKEN("만료된 액세스 토큰입니다.", HttpStatus.UNAUTHORIZED, "G_011"),

	// 500
	INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "G_009");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
