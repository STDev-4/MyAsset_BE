package io.api.myasset.domain.approval.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.api.myasset.domain.approval.persistence.CardApproval;
import io.codef.api.dto.EasyCodefResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CODEF 카드 승인내역 응답을 {@link CardApproval} 엔티티 리스트로 변환한다.
 * ApprovalService 와 CodefSyncJob 의 Processor 가 공유한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardApprovalParser {

	private final ObjectMapper objectMapper;

	/**
	 * CODEF 응답을 파싱한다. 데이터가 없으면 빈 리스트를 반환한다 (예외 X).
	 * Batch 환경에서는 특정 유저의 일부 기관 응답이 비어 있어도 Job 이 진행돼야 하기 때문이다.
	 */
	public List<CardApproval> parse(EasyCodefResponse response, String connectedId) {
		if (response == null || response.getData() == null) {
			return Collections.emptyList();
		}

		List<Map<String, String>> dataList = objectMapper.convertValue(
			response.getData(),
			new TypeReference<List<Map<String, String>>>() {});

		if (dataList == null || dataList.isEmpty()) {
			return Collections.emptyList();
		}

		return dataList.stream()
			.map(record -> toEntity(record, connectedId))
			.filter(java.util.Objects::nonNull)
			.toList();
	}

	private CardApproval toEntity(Map<String, String> record, String connectedId) {
		try {
			String merchantType = record.get("resMemberStoreType");
			String approvalDate = record.get("resUsedDate");
			String amountStr = record.get("resUsedAmount");

			if (merchantType == null || approvalDate == null || amountStr == null) {
				return null;
			}

			return CardApproval.builder()
				.merchantType(merchantType)
				.approvalDate(approvalDate)
				.approvalAmount(Long.parseLong(amountStr))
				.connectedId(connectedId)
				.build();
		} catch (NumberFormatException e) {
			log.warn("[CardApprovalParser] 금액 파싱 실패 - connectedId={}, record={}",
				connectedId, record);
			return null;
		}
	}
}
