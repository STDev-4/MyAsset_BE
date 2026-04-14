package io.api.myasset.global.exception.error;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.validation.FieldError;

public record ErrorResponse(
	String timeStamp,
	String errorCode,
	String errorMessage,
	Object details) {

	public ErrorResponse(ErrorCode errorCode) {
		this(
			LocalDateTime.now().toString(),
			errorCode.getCode(),
			errorCode.getMessage(),
			null);
	}

	public ErrorResponse(ErrorCode errorCode, Object details) {
		this(
			LocalDateTime.now().toString(),
			errorCode.getCode(),
			errorCode.getMessage(),
			details);
	}

	public ErrorResponse(FieldError fieldError) {
		this(
			LocalDateTime.now().toString(),
			fieldError != null ? fieldError.getCode() : "",
			fieldError != null ? fieldError.getDefaultMessage() : "",
			null);
	}

	public ErrorResponse(List<FieldError> fieldErrors) {
		this(
			GlobalError.NOT_VALID_EXCEPTION,
			fieldErrors.stream()
				.collect(Collectors.toMap(
					FieldError::getField,
					fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "null",
					(existing, replacement) -> replacement)));
	}
}
