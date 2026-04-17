package io.api.myasset.domain.approval.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CardApprovalRaw(
	String resMemberStoreName,
	String resUsedDate,
	String resUsedAmount) {
}
