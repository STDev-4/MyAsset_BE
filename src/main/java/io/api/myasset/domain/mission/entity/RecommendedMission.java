package io.api.myasset.domain.mission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recommended_mission")
public class RecommendedMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_type", nullable = false, length = 30)
    private String iconType;

    @Column(name = "reward_point", nullable = false)
    private Integer rewardPoint;

    @Column(name = "expected_saving_amount", nullable = false)
    private Integer expectedSavingAmount;

    @Column(name = "behavior_insights_json", nullable = false, columnDefinition = "TEXT")
    private String behaviorInsightsJson;

    @Column(name = "statistical_reasons_json", nullable = false, columnDefinition = "TEXT")
    private String statisticalReasonsJson;

    @Column(nullable = false)
    private boolean accepted;

    @Column(name = "recommend_date", nullable = false)
    private LocalDate recommendDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder(builderMethodName = "internalBuilder")
    private RecommendedMission(
            Long userId,
            String title,
            String description,
            String iconType,
            Integer rewardPoint,
            Integer expectedSavingAmount,
            String behaviorInsightsJson,
            String statisticalReasonsJson,
            boolean accepted,
            LocalDate recommendDate
    ) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.iconType = iconType;
        this.rewardPoint = rewardPoint;
        this.expectedSavingAmount = expectedSavingAmount;
        this.behaviorInsightsJson = behaviorInsightsJson;
        this.statisticalReasonsJson = statisticalReasonsJson;
        this.accepted = accepted;
        this.recommendDate = recommendDate;
    }

    public static RecommendedMission of(
            Long userId,
            String title,
            String description,
            String iconType,
            Integer rewardPoint,
            Integer expectedSavingAmount,
            String behaviorInsightsJson,
            String statisticalReasonsJson,
            LocalDate recommendDate
    ) {
        return RecommendedMission.internalBuilder()
                .userId(userId)
                .title(title)
                .description(description)
                .iconType(iconType)
                .rewardPoint(rewardPoint)
                .expectedSavingAmount(expectedSavingAmount)
                .behaviorInsightsJson(behaviorInsightsJson)
                .statisticalReasonsJson(statisticalReasonsJson)
                .accepted(false)
                .recommendDate(recommendDate)
                .build();
    }

    public void accept() {
        this.accepted = true;
    }
}