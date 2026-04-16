package io.api.myasset.global.batch.event;

/**
 * 유저가 금융기관(은행/증권/카드) 연동에 성공했음을 알리는 이벤트.
 * <p>
 * 발행: {@code CodefLinkService.link()} 성공 커밋 후
 * 구독: {@code CodefSyncJobLauncher} (AFTER_COMMIT) → 해당 유저 Job 즉시 실행
 * <p>
 * CODEF 도메인과 Batch 도메인을 느슨하게 분리하기 위한 메시지 오브젝트다.
 */
public record BankLinkedEvent(Long userId) {
}
