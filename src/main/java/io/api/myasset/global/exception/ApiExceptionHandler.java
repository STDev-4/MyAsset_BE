package io.api.myasset.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.api.myasset.global.exception.error.BusinessException;
import io.api.myasset.global.exception.error.ErrorResponse;
import io.api.myasset.global.exception.error.ErrorCode;
import io.api.myasset.global.exception.error.GlobalError;
import io.codef.api.error.CodefException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
		return new ErrorResponse(e.getBindingResult().getFieldErrors());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ErrorResponse handleMissingParameterException() {
		return new ErrorResponse(GlobalError.MISSING_REQUEST_PARAMETER);
	}

	@ExceptionHandler(CodefException.class)
	public ResponseEntity<ErrorResponse> handleCodefException(final CodefException e) {
		log.error("Codef Error = {} / {}", e.getCodefError().name(), e.getMessage());
		return convert(GlobalError.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(final BusinessException e) {
		logGeneralException(e);
		return convert(e.getErrorCode());
	}

	private ResponseEntity<ErrorResponse> convert(final ErrorCode errorCode) {
		return ResponseEntity.status(errorCode.getStatus())
			.body(new ErrorResponse(errorCode));
	}

	private void logGeneralException(final BusinessException e) {
		if (e.getErrorCode().getStatus().is5xxServerError()) {
			log.error("", e);
		} else {
			log.error("Error Message = {}", e.getMessage());
		}
	}
}
