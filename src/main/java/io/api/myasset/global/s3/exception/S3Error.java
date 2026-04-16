package io.api.myasset.global.s3.exception;

import org.springframework.http.HttpStatus;

import io.api.myasset.global.exception.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum S3Error implements ErrorCode {

	EMPTY_FILE("파일이 비어 있습니다.", HttpStatus.BAD_REQUEST, "S3_001"),
	EMPTY_KEY("key가 비어 있습니다.", HttpStatus.BAD_REQUEST, "S3_002"),
	NOT_FOUND("S3에 해당 파일이 존재하지 않습니다.", HttpStatus.NOT_FOUND, "S3_003"),
	IO_ERROR("파일 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "S3_004"),
	UPLOAD_FAILED("S3 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "S3_005"),
	DELETE_FAILED("S3 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "S3_006");

	private final String message;
	private final HttpStatus status;
	private final String code;
}
