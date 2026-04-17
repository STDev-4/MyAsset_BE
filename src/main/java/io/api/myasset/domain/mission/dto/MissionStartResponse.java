package io.api.myasset.domain.mission.dto;

import java.time.LocalDateTime;

public record MissionStartResponse(
        Long missionId,
        String status,
        LocalDateTime startedAt
) {
}