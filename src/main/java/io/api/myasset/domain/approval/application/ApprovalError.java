package io.api.myasset.domain.approval.application;

import org.springframework.http.HttpStatus;

import io.api.myasset.global.exception.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApprovalError implements ErrorCode {

	CODEF_API_FAILED("카드 승인 내역 조회에 실패했습니다.", HttpStatus.BAD_GATEWAY, "S_001"),
	NO_APPROVAL_DATA("승인 내역 데이터가 없습니다.", HttpStatus.NOT_FOUND, "S_002");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
