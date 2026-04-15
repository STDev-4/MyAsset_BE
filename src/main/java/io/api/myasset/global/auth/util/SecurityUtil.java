package io.api.myasset.global.auth.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.global.exception.error.BusinessException;

/**
 * SecurityContext에서 인증된 사용자 정보를 조회하는 유틸리티
 */
public class SecurityUtil {

	private SecurityUtil() {}

	/**
	 * 현재 인증된 사용자 ID 조회
	 * JwtAuthenticationFilter에서 principal로 userId(Long)를 설정하므로 Long 캐스팅
	 */
	public static Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null
			|| !authentication.isAuthenticated()
			|| authentication instanceof AnonymousAuthenticationToken) {
			throw new BusinessException(UserError.UNAUTHORIZED);
		}

		Object principal = authentication.getPrincipal();

		if (principal instanceof Long userId) {
			return userId;
		}

		throw new BusinessException(UserError.UNAUTHORIZED);
	}
}
