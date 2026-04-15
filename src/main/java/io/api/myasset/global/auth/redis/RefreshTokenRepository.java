package io.api.myasset.global.auth.redis;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

	private static final String KEY_PREFIX = "RT:";

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * 리프레시 토큰 저장
	 * Key: RT:{userId} → Value: refreshToken
	 * 같은 userId로 재로그인 시 기존 토큰 자동 덮어씀 (단일 세션)
	 */
	public void save(Long userId, String refreshToken, long expiryMs) {
		redisTemplate.opsForValue()
			.set(buildKey(userId), refreshToken, expiryMs, TimeUnit.MILLISECONDS);
		log.info("Refresh Token 저장 - User ID: {}", userId);
	}

	/** userId로 저장된 리프레시 토큰 조회 */
	public Optional<String> findByUserId(Long userId) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(buildKey(userId)));
	}

	/**
	 * Redis 저장 토큰과 요청 토큰 비교 검증
	 */
	public boolean validateRefreshToken(Long userId, String refreshToken) {
		return findByUserId(userId)
			.map(stored -> stored.equals(refreshToken))
			.orElse(false);
	}

	/** 리프레시 토큰 삭제 (로그아웃, 토큰 갱신 후 기존 토큰 제거) */
	public void delete(Long userId) {
		redisTemplate.delete(buildKey(userId));
		log.info("Refresh Token 삭제 - User ID: {}", userId);
	}

	private String buildKey(Long userId) {
		return KEY_PREFIX + userId;
	}
}
