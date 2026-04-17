package io.api.myasset.domain.analysisinsight.exception;

import io.api.myasset.global.exception.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalysisError implements ErrorCode {

    ANALYSIS_INSIGHT_NOT_FOUND("행동과학 인사이트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "A_001"),
    ACTION_TIPS_JSON_PARSE_ERROR("actionTipsJson 파싱에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "A_002"),
    ACTION_TIPS_JSON_SERIALIZE_ERROR("actionTipsJson 변환에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "A_003"),
    INSIGHT_REDIS_SERIALIZE_ERROR("인사이트 캐시 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "A_004"),
    INSIGHT_REDIS_DESERIALIZE_ERROR("인사이트 캐시 조회에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "A_005");

    private final String message;
    private final HttpStatus status;
    private final String code;
}