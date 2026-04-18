package io.api.myasset.domain.home.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeSummaryResponse {

    private String currentImageName;
    private String tier;
    private Integer point;
    private String currentImageUrl;

    private Integer totalMissionCount;// 오늘 미션 수
    private Integer completedMissionCount;
    private Integer successRate;

    public static HomeSummaryResponse of(
            String currentImageName,
            String tier,
            Integer point,
            String currentImageUrl,
            Integer totalMissionCount,
            Integer completedMissionCount,
            Integer successRate
    ) {
        return HomeSummaryResponse.builder()
                .currentImageName(currentImageName)
                .tier(tier)
                .point(point)
                .currentImageUrl(currentImageUrl)
                .totalMissionCount(totalMissionCount)
                .completedMissionCount(completedMissionCount)
                .successRate(successRate)
                .build();
    }
}