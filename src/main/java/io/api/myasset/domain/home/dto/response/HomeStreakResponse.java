package io.api.myasset.domain.home.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

//연속 절약 일수 조회 dto
@Getter
@Builder
public class HomeStreakResponse {

	private final Integer streakDays;
	private final List<Boolean> weekProgress;
	private final Integer todayIndex;
}
