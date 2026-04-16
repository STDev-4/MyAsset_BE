package io.api.myasset.global.exception;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.api.myasset.global.config.ErrorResponseWriter;
import io.api.myasset.global.exception.error.BusinessException;
import io.api.myasset.global.exception.error.GlobalError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiExceptionHandlingFilter extends OncePerRequestFilter {

	private final ErrorResponseWriter errorResponseWriter;

	@Override
	protected void doFilterInternal(
		final @NonNull HttpServletRequest request,
		final @NonNull HttpServletResponse response,
		final FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (final BusinessException e) {
			if (!response.isCommitted()) {
				errorResponseWriter.write(response, e.getErrorCode().getStatus(), e.getErrorCode());
			}
		} catch (final Exception e) {
			log.error("필터 체인에서 예상치 못한 예외 발생 - URI: {}, message: {}", request.getRequestURI(), e.getMessage(), e);
			if (!response.isCommitted()) {
				errorResponseWriter.write(response, HttpStatus.INTERNAL_SERVER_ERROR,
					GlobalError.INTERNAL_SERVER_ERROR);
			}
		}
	}
}
