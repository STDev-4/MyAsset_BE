package io.api.myasset.domain.league.exception;

import org.springframework.http.HttpStatus;

import io.api.myasset.global.exception.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeagueErrorCode implements ErrorCode {

    LEAGUE_USER_NOT_FOUND("리그 사용자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "LEAGUE_001"),
    LEAGUE_RANKING_NOT_FOUND("리그 랭킹 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "LEAGUE_002"),
    INVALID_LEAGUE_TIER("유효하지 않은 리그 티어입니다.", HttpStatus.BAD_REQUEST, "LEAGUE_003");

    private final String message;
    private final HttpStatus status;
    private final String code;
}