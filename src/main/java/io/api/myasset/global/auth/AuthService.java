package io.api.myasset.global.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.user.domain.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.auth.dto.LoginRequest;
import io.api.myasset.global.auth.jwt.JwtProperties;
import io.api.myasset.global.auth.jwt.JwtProvider;
import io.api.myasset.global.auth.redis.RefreshTokenRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final JwtProperties jwtProperties;
	private final RefreshTokenRepository refreshTokenRepository;

	public record TokenPair(String accessToken, String refreshToken) {
	}

	public TokenPair login(LoginRequest request) {
		User user = userRepository.findByLoginId(request.loginId())
			.orElseThrow(() -> new BusinessException(UserError.INVALID_PASSWORD));

		if (!user.matchesPassword(request.password(), passwordEncoder)) {
			throw new BusinessException(UserError.INVALID_PASSWORD);
		}

		return issueTokens(user);
	}

	/**
	 * 리프레시 토큰 갱신 (rotation)
	 * 1. JWT 서명 검증
	 * 2. Redis 저장 토큰과 비교
	 * 3. 신규 토큰 발급 + 기존 토큰 삭제
	 */
	@Transactional
	public TokenPair refresh(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new BusinessException(UserError.INVALID_REFRESH_TOKEN);
		}

		if (!jwtProvider.validateToken(refreshToken)) {
			throw new BusinessException(UserError.INVALID_REFRESH_TOKEN);
		}

		Long userId = jwtProvider.getUserIdFromToken(refreshToken);

		if (!refreshTokenRepository.validateRefreshToken(userId, refreshToken)) {
			throw new BusinessException(UserError.INVALID_REFRESH_TOKEN);
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		refreshTokenRepository.delete(userId);
		return issueTokens(user);
	}

	@Transactional
	public void logout(Long userId) {
		refreshTokenRepository.delete(userId);
		log.info("로그아웃 - User ID: {}", userId);
	}

	private TokenPair issueTokens(User user) {
		String accessToken = jwtProvider.generateAccessToken(user);
		String refreshToken = jwtProvider.generateRefreshToken(user);
		refreshTokenRepository.save(user.getId(), refreshToken, jwtProperties.getRefreshTokenExpiry());
		return new TokenPair(accessToken, refreshToken);
	}
}
