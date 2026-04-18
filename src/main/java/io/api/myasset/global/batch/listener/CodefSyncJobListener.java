package io.api.myasset.global.batch.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import io.api.myasset.domain.approval.application.SyncStatusService;
import io.api.myasset.global.batch.job.codefsync.CodefSyncJobConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CodefSyncJob 종료 시 유저별 {@link io.api.myasset.domain.approval.application.SyncStatus} 를 갱신.
 * <p>
 * JobParameters 에 userId 가 있을 때만 (= 이벤트 기반 단건 실행) 상태를 기록한다.
 * 스케줄러에 의한 전체 실행에서는 유저별 polling 이 없으므로 상태 갱신 불필요.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodefSyncJobListener implements JobExecutionListener {

	private final SyncStatusService syncStatusService;

	@Override
	public void afterJob(JobExecution jobExecution) {
		Long userId = jobExecution.getJobParameters().getLong(CodefSyncJobConfig.PARAM_USER_ID);
		if (userId == null) {
			return;
		}

		BatchStatus status = jobExecution.getStatus();
		if (status == BatchStatus.COMPLETED) {
			syncStatusService.markCompleted(userId);
			log.info("[CodefSyncJob] afterJob - userId={}, SyncStatus=COMPLETED", userId);
		} else {
			syncStatusService.markFailed(userId);
			log.warn("[CodefSyncJob] afterJob - userId={}, BatchStatus={}, SyncStatus=FAILED",
				userId, status);
		}
	}
}
