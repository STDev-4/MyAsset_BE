package io.api.myasset.domain.tier.exception;

import org.springframework.http.HttpStatus;

import io.api.myasset.global.exception.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TierError implements ErrorCode {

	INVALID_TIER("존재하지 않는 티어입니다.", HttpStatus.BAD_REQUEST, "T_001");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
