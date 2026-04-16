package io.api.myasset.domain.mission.dto;

import jakarta.validation.constraints.NotNull;

public record MissionAcceptRequest(
        @NotNull
        Long missionId
) {
}