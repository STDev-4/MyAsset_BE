package io.api.myasset.domain.gpt.exception;


import io.api.myasset.global.exception.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GptErrorCode implements ErrorCode {

    GPT_TOO_MANY_REQUESTS(
            HttpStatus.TOO_MANY_REQUESTS,
            "GPT-001",
            "GPT 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
    ),

    GPT_API_ERROR(
            HttpStatus.BAD_GATEWAY,
            "GPT-002",
            "GPT API 호출 중 오류가 발생했습니다."
    ),

    GPT_TIMEOUT(
            HttpStatus.GATEWAY_TIMEOUT,
            "GPT-003",
            "GPT 응답 시간이 초과되었습니다."
    ),

    GPT_UNKNOWN_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "GPT-004",
            "GPT 처리 중 알 수 없는 오류가 발생했습니다."
    ),
    GPT_RESPONSE_FORMAT_ERROR(
            HttpStatus.BAD_GATEWAY,
            "GPT-005",
            "GPT 응답 형식이 올바르지 않습니다."
    ),

    GPT_PARSE_ERROR(
            HttpStatus.BAD_GATEWAY,
            "GPT-006",
            "GPT 응답 JSON 파싱에 실패했습니다."
    );
    private final HttpStatus Status;
    private final String code;
    private final String message;
}
