package io.api.myasset.domain.approval.application;

/**
 * CodefSyncJob 의 현재 유저별 진행 상태.
 * <p>
 * FE LoadingCompletePage 가 polling 으로 확인하여 HomePage 전환 여부 결정.
 */
public enum SyncStatus {

	/** 연동 후 아직 Job 이 시작되지 않았거나 TTL 만료로 상태 소실 */
	NOT_STARTED,

	/** Job 진행 중 - 계속 polling 필요 */
	IN_PROGRESS,

	/** Job 완료 - 소비 데이터 및 캐시가 사용 가능 */
	COMPLETED,

	/** Job 실패 - FE 는 에러 화면 또는 재시도 UI 표시 */
	FAILED
}
