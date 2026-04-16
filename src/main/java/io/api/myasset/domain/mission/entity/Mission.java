package io.api.myasset.domain.mission.entity;

import io.api.myasset.domain.mission.enums.MissionStatus;
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
@Table(name = "mission")
public class Mission {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionStatus status;

    @Column(name = "mission_date", nullable = false)
    private LocalDate missionDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "auto_evaluate_at", nullable = false)
    private LocalDateTime autoEvaluateAt;

    @Column(name = "recommended_mission_id", nullable = false)
    private Long recommendedMissionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder(builderMethodName = "internalBuilder")
    private Mission(
            Long userId,
            String title,
            String description,
            String iconType,
            Integer rewardPoint,
            Integer expectedSavingAmount,
            String behaviorInsightsJson,
            String statisticalReasonsJson,
            MissionStatus status,
            LocalDate missionDate,
            LocalDateTime startedAt,
            LocalDateTime autoEvaluateAt,
            Long recommendedMissionId
    ) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.iconType = iconType;
        this.rewardPoint = rewardPoint;
        this.expectedSavingAmount = expectedSavingAmount;
        this.behaviorInsightsJson = behaviorInsightsJson;
        this.statisticalReasonsJson = statisticalReasonsJson;
        this.status = status;
        this.missionDate = missionDate;
        this.startedAt = startedAt;
        this.autoEvaluateAt = autoEvaluateAt;
        this.recommendedMissionId = recommendedMissionId;
    }

    public static Mission from(RecommendedMission recommendedMission) {
        LocalDate today = LocalDate.now();

        return Mission.internalBuilder()
                .userId(recommendedMission.getUserId())
                .title(recommendedMission.getTitle())
                .description(recommendedMission.getDescription())
                .iconType(recommendedMission.getIconType())
                .rewardPoint(recommendedMission.getRewardPoint())
                .expectedSavingAmount(recommendedMission.getExpectedSavingAmount())
                .behaviorInsightsJson(recommendedMission.getBehaviorInsightsJson())
                .statisticalReasonsJson(recommendedMission.getStatisticalReasonsJson())
                .status(MissionStatus.READY)
                .missionDate(today)
                .startedAt(null)
                .autoEvaluateAt(today.atTime(23, 59, 59))
                .recommendedMissionId(recommendedMission.getId())
                .build();
    }

    public void start() {
        this.status = MissionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }
}