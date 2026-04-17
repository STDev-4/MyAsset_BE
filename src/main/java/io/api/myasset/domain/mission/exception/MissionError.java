package io.api.myasset.domain.mission.exception;

import io.api.myasset.global.exception.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MissionError implements ErrorCode {

    RECOMMENDED_MISSION_NOT_FOUND("추천 미션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "M_001"),
    MISSION_NOT_FOUND("미션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "M_002"),
    MISSION_ALREADY_ACCEPTED("이미 수락한 추천 미션입니다.", HttpStatus.BAD_REQUEST, "M_003"),
    INVALID_MISSION_STATUS("현재 상태에서는 미션을 시작할 수 없습니다.", HttpStatus.BAD_REQUEST, "M_004"),
    MISSION_REDIS_SERIALIZE_ERROR("추천 미션 캐시 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "M_005"),
    MISSION_REDIS_DESERIALIZE_ERROR("추천 미션 캐시 조회에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "M_006");

    private final String message;
    private final HttpStatus status;
    private final String code;
}