package io.api.myasset.domain.home.dto.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeRankingPercentileResponse {

    private String rankingPercentile;
	private final LocalDate measuredAt;
}
