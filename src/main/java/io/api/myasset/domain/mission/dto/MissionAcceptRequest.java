package io.api.myasset.domain.mission.dto;

import jakarta.validation.constraints.NotBlank;

public record MissionAcceptRequest(
        @NotBlank
        String recommendationId
) {
}