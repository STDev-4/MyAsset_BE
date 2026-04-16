package io.api.myasset.global.batch.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.api.myasset.global.batch.launcher.CodefSyncJobLauncher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CodefSyncJob 정기 실행 스케줄러.
 * <p>
 * 매일 04:00 에 connectedId 를 가진 전체 유저의 소비 데이터를 선제적으로 수집한다.
 * 로컬/개발 환경에서는 {@code batch.scheduler.enabled=false} (기본값) 로 비활성화한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "batch.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class CodefSyncScheduler {

	private final CodefSyncJobLauncher jobLauncher;

	@Scheduled(cron = "0 0 4 * * *")
	public void runDailySync() {
		log.info("[CodefSyncScheduler] 정기 실행 시작");
		jobLauncher.launchForAll();
	}
}
