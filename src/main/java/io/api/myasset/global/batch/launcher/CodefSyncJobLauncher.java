package io.api.myasset.global.batch.launcher;

import java.time.Instant;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.api.myasset.domain.approval.application.SyncStatusService;
import io.api.myasset.global.batch.event.BankLinkedEvent;
import io.api.myasset.global.batch.job.codefsync.CodefSyncJobConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * CodefSyncJob 기동 전담 컴포넌트.
 * <p>
 * - 스케줄러: {@link #launchForAll()} — 전체 유저 야간 배치
 * - 신규 연동: {@link #onBankLinked(BankLinkedEvent)} — CodefLinkService 커밋 이후 단건 실행
 * <p>
 * runAt 파라미터로 매 실행마다 새 JobInstance 를 보장해
 * {@code JobInstanceAlreadyCompleteException} 을 회피한다.
 */
@Slf4j
@Component
public class CodefSyncJobLauncher {

	private final JobOperator jobOperator;
	private final Job codefSyncJob;
	private final SyncStatusService syncStatusService;

	/**
	 * Lombok 이 {@code @Qualifier} 를 생성자 파라미터로 복사하지 않는 경우에 대비해
	 * 명시적으로 생성자를 선언한다.
	 */
	public CodefSyncJobLauncher(
		@Qualifier("asyncJobOperator")
		JobOperator jobOperator,
		@Qualifier(CodefSyncJobConfig.JOB_NAME)
		Job codefSyncJob,
		SyncStatusService syncStatusService) {
		this.jobOperator = jobOperator;
		this.codefSyncJob = codefSyncJob;
		this.syncStatusService = syncStatusService;
	}

	public void launchForAll() {
		launch(null);
	}

	public void launchForUser(Long userId) {
		launch(userId);
	}

	/**
	 * BankLinkedEvent 수신 시 단건 Job 을 실행한다.
	 * Job 은 내부적으로 Step 1 (CODEF → DB) + Step 2 (DB → Redis warm) 를 순차 수행하므로,
	 * 이 한 번의 호출로 신규 유저의 소비 데이터 적재 + 캐시 pre-warm 이 모두 완료된다.
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onBankLinked(BankLinkedEvent event) {
		log.info("[CodefSyncJob] BankLinkedEvent 수신, 단건 Sync + Cache warm 실행 예약 - userId={}",
			event.userId());
		syncStatusService.markInProgress(event.userId());
		launchForUser(event.userId());
	}

	private void launch(Long userId) {
		JobParametersBuilder builder = new JobParametersBuilder()
			.addLong(CodefSyncJobConfig.PARAM_RUN_AT, Instant.now().toEpochMilli());

		if (userId != null) {
			builder.addLong(CodefSyncJobConfig.PARAM_USER_ID, userId);
		}

		JobParameters params = builder.toJobParameters();

		try {
			jobOperator.start(codefSyncJob, params);
			log.info("[CodefSyncJob] 기동 성공 - userId={}", userId);
		} catch (Exception e) {
			log.error("[CodefSyncJob] 기동 실패 - userId={}, reason={}",
				userId, e.getMessage(), e);
			if (userId != null) {
				syncStatusService.markFailed(userId);
			}
		}
	}
}
