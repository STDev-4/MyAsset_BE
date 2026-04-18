package io.api.myasset.domain.approval.application;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 유저별 CodefSyncJob 진행 상태를 Redis 에 기록/조회.
 * <p>
 * - Key: {@code codef:sync:status:{userId}}
 * - Value: {@link SyncStatus} 이름 (IN_PROGRESS / COMPLETED / FAILED)
 * - TTL: IN_PROGRESS 10분 (Job 무응답 보호), COMPLETED/FAILED 5분
 * <p>
 * Redis 장애 시 상태 조회는 {@link SyncStatus#NOT_STARTED} 로 폴백 (fail-open).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncStatusService {

	private static final String KEY_PREFIX = "codef:sync:status";
	private static final Duration TTL_IN_PROGRESS = Duration.ofMinutes(10);
	private static final Duration TTL_FINAL = Duration.ofMinutes(5);

	private final RedisTemplate<String, String> redisTemplate;

	public void markInProgress(Long userId) {
		set(userId, SyncStatus.IN_PROGRESS, TTL_IN_PROGRESS);
	}

	public void markCompleted(Long userId) {
		set(userId, SyncStatus.COMPLETED, TTL_FINAL);
	}

	public void markFailed(Long userId) {
		set(userId, SyncStatus.FAILED, TTL_FINAL);
	}

	public SyncStatus get(Long userId) {
		try {
			String value = redisTemplate.opsForValue().get(key(userId));
			return (value == null) ? SyncStatus.NOT_STARTED : SyncStatus.valueOf(value);
		} catch (IllegalArgumentException e) {
			log.warn("[SyncStatus] 알 수 없는 상태 값, NOT_STARTED 로 폴백 - userId={}", userId);
			return SyncStatus.NOT_STARTED;
		} catch (Exception e) {
			log.warn("[SyncStatus] Redis 조회 실패, NOT_STARTED 로 폴백 - userId={}, reason={}",
				userId, e.getMessage());
			return SyncStatus.NOT_STARTED;
		}
	}

	private void set(Long userId, SyncStatus status, Duration ttl) {
		try {
			redisTemplate.opsForValue().set(key(userId), status.name(), ttl);
			log.debug("[SyncStatus] 상태 변경 - userId={}, status={}", userId, status);
		} catch (Exception e) {
			log.warn("[SyncStatus] Redis 기록 실패 - userId={}, status={}, reason={}",
				userId, status, e.getMessage());
		}
	}

	private String key(Long userId) {
		return "%s:%d".formatted(KEY_PREFIX, userId);
	}
}
