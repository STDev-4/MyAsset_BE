package io.api.myasset.domain.mission.dto;

import jakarta.validation.constraints.NotBlank;

public record MissionStartRequest(
        @NotBlank
        String recommendationId
) {
}