package io.api.myasset.domain.analysisinsight.entity;

import io.api.myasset.domain.analysisinsight.enums.InsightColorType;
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
@Table(name = "analysis_insight")
public class AnalysisInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "color_type", nullable = false, length = 20)
    private InsightColorType colorType;

    @Column(name = "action_tips_json", nullable = false, columnDefinition = "TEXT")
    private String actionTipsJson;

    @Column(name = "insight_date", nullable = false)
    private LocalDate insightDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder(builderMethodName = "internalBuilder")
    private AnalysisInsight(
            Long userId,
            String title,
            String description,
            InsightColorType colorType,
            String actionTipsJson,
            LocalDate insightDate
    ) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.colorType = colorType;
        this.actionTipsJson = actionTipsJson;
        this.insightDate = insightDate;
    }

    public static AnalysisInsight of(
            Long userId,
            String title,
            String description,
            InsightColorType colorType,
            String actionTipsJson,
            LocalDate insightDate
    ) {
        return AnalysisInsight.internalBuilder()
                .userId(userId)
                .title(title)
                .description(description)
                .colorType(colorType)
                .actionTipsJson(actionTipsJson)
                .insightDate(insightDate)
                .build();
    }

    public void update(
            String title,
            String description,
            InsightColorType colorType,
            String actionTipsJson
    ) {
        this.title = title;
        this.description = description;
        this.colorType = colorType;
        this.actionTipsJson = actionTipsJson;
    }
}