package io.api.myasset.domain.approval.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CODEF 카드 승인내역 raw 레코드.
 * <p>
 * {@code merchantType} 은 CODEF 의 {@code resMemberStoreType} (가맹점 업종) 을 저장한다.
 * 예: "음식점", "편의점", "카페", "주유소" 등. 카테고리 단위 집계에 사용된다.
 */
@Entity
@Table(name = "card_approval", uniqueConstraints = @UniqueConstraint(name = "uk_card_approval_dedup", columnNames = {
	"connectedId", "approvalDate", "merchantType", "approvalAmount"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardApproval {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String merchantType;

	@Column(nullable = false)
	private long approvalAmount;

	@Column(nullable = false, length = 8)
	private String approvalDate;

	@Column(nullable = false, length = 100)
	private String connectedId;

	@Builder
	public CardApproval(
		String merchantType,
		long approvalAmount,
		String approvalDate,
		String connectedId) {
		this.merchantType = merchantType;
		this.approvalAmount = approvalAmount;
		this.approvalDate = approvalDate;
		this.connectedId = connectedId;
	}
}
