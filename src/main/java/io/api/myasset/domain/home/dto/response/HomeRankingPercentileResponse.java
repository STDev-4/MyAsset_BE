package io.api.myasset.domain.home.dto.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeRankingPercentileResponse {

	private final double  rankingPercentile;
	private final LocalDate measuredAt;
}
