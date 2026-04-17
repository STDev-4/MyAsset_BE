package io.api.myasset.global.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.auth.dto.LoginRequest;
import io.api.myasset.global.auth.dto.SignupRequest;
import io.api.myasset.global.auth.dto.SignupResponse;
import io.api.myasset.global.auth.jwt.JwtProperties;
import io.api.myasset.global.auth.jwt.JwtProvider;
import io.api.myasset.global.auth.redis.RefreshTokenRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

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

	public record SignupResult(SignupResponse userInfo, TokenPair tokens) {
	}

	@Transactional
	public SignupResult signup(SignupRequest request) {
		if (userRepository.existsByLoginId(request.loginId())) {
			throw new BusinessException(UserError.DUPLICATE_LOGIN_ID);
		}
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException(UserError.DUPLICATE_EMAIL);
		}

		User user = User.create(
			request.loginId(),
			passwordEncoder.encode(request.password()),
			request.email(),
			request.nickname(),
			request.birthDate());

        user.updateLastLoginAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        TokenPair tokens = issueTokens(saved);

		return new SignupResult(SignupResponse.from(saved), tokens);
	}

    @Transactional
	public TokenPair login(LoginRequest request) {
		User user = userRepository.findByLoginId(request.loginId())
			.orElseThrow(() -> new BusinessException(UserError.INVALID_PASSWORD));

		if (!user.matchesPassword(request.password(), passwordEncoder)) {
			throw new BusinessException(UserError.INVALID_PASSWORD);
		}

        // 로그인 성공 시 lastLoginAt 갱신
        user.updateLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

		return issueTokens(user);
	}

	@Transactional
	public TokenPair refresh(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new BusinessException(UserError.INVALID_REFRESH_TOKEN);
		}

		if (!jwtProvider.validateToken(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
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
