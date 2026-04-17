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

	/**
	 * Lombok 이 {@code @Qualifier} 를 생성자 파라미터로 복사하지 않는 경우에 대비해
	 * 명시적으로 생성자를 선언한다.
	 */
	public CodefSyncJobLauncher(
		@Qualifier("asyncJobOperator")
		JobOperator jobOperator,
		@Qualifier(CodefSyncJobConfig.JOB_NAME)
		Job codefSyncJob) {
		this.jobOperator = jobOperator;
		this.codefSyncJob = codefSyncJob;
	}

	public void launchForAll() {
		launch(null);
	}

	public void launchForUser(Long userId) {
		launch(userId);
	}

	/**
	 * CodefLinkService 가 발행한 BankLinkedEvent 를 트랜잭션 커밋 이후 수신해 Job 을 실행한다.
	 * 커밋 이전 실행 시 Job 이 아직 저장되지 않은 connectedId 를 보게 되므로 AFTER_COMMIT 필수.
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onBankLinked(BankLinkedEvent event) {
		log.info("[CodefSyncJob] BankLinkedEvent 수신 - userId={}", event.userId());
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
		}
	}
}
