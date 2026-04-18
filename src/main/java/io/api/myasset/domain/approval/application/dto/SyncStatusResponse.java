package io.api.myasset.domain.approval.application.dto;

import io.api.myasset.domain.approval.application.SyncStatus;

/**
 * {@code GET /api/codef/sync/status} 응답.
 */
public record SyncStatusResponse(SyncStatus status) {

	public static SyncStatusResponse of(SyncStatus status) {
		return new SyncStatusResponse(status);
	}
}
