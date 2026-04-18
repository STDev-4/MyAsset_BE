package io.api.myasset.global.batch.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import io.api.myasset.domain.analysisinsight.service.AnalysisInsightService;
import io.api.myasset.domain.approval.application.SyncStatusService;
import io.api.myasset.domain.mission.service.RecommendedMissionService;
import io.api.myasset.global.batch.job.codefsync.CodefSyncJobConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CodefSyncJob 종료 시 후처리.
 * <ul>
 *   <li>단건 실행 (BankLinkedEvent → 신규 유저) 경로에서만 동작</li>
 *   <li>Job 성공 시: GPT 인사이트/추천 미션 선제 생성 → {@code SyncStatus} COMPLETED 마킹</li>
 *   <li>Job 실패 시: FAILED 마킹만</li>
 * </ul>
 * <p>
 * <b>순서가 중요</b>: GPT 선제 생성 후에 COMPLETED 를 마킹해야 FE LoadingCompletePage
 * polling 이 COMPLETED 를 받은 시점엔 이미 GPT 캐시가 채워져 있다.
 * 결과적으로 사용자가 HomePage 진입 후 분석 탭을 바로 눌러도 cache hit → 즉시 표시.
 * <p>
 * GPT 호출 자체가 실패해도 COMPLETED 는 마킹 (배치 자체는 성공이므로). 분석 탭은 lazy 경로로
 * 자동 복구된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodefSyncJobListener implements JobExecutionListener {

	private final SyncStatusService syncStatusService;
	private final AnalysisInsightService analysisInsightService;
	private final RecommendedMissionService recommendedMissionService;

	@Override
	public void afterJob(JobExecution jobExecution) {
		Long userId = jobExecution.getJobParameters().getLong(CodefSyncJobConfig.PARAM_USER_ID);
		if (userId == null) {
			return;
		}

		BatchStatus status = jobExecution.getStatus();
		if (status != BatchStatus.COMPLETED) {
			syncStatusService.markFailed(userId);
			log.warn("[CodefSyncJob] afterJob - userId={}, BatchStatus={}, SyncStatus=FAILED",
				userId, status);
			return;
		}

		log.info("[CodefSyncJob] afterJob - userId={}, GPT 선제 생성 시작", userId);
		preGenerateGptContent(userId);

		syncStatusService.markCompleted(userId);
		log.info("[CodefSyncJob] afterJob - userId={}, SyncStatus=COMPLETED", userId);
	}

	/**
	 * GPT 기반 인사이트 / 추천 미션 캐시를 선제 생성.
	 * 실패해도 Job 전체 완료 판정엔 영향 없음 (lazy 폴백 경로가 런타임에 재시도).
	 */
	private void preGenerateGptContent(Long userId) {
		try {
			analysisInsightService.getInsights(userId);
		} catch (Exception e) {
			log.warn("[CodefSyncJob] GPT insights 선제 생성 실패 - userId={}, reason={}",
				userId, e.getMessage());
		}
		try {
			recommendedMissionService.getRecommendedMissions(userId);
		} catch (Exception e) {
			log.warn("[CodefSyncJob] GPT recommended-missions 선제 생성 실패 - userId={}, reason={}",
				userId, e.getMessage());
		}
	}
}
