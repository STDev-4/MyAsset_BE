package io.api.myasset.domain.analysisinsight.repository;

import io.api.myasset.domain.analysisinsight.entity.AnalysisInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AnalysisInsightRepository extends JpaRepository<AnalysisInsight, Long> {

    @Query("""
            select ai
            from AnalysisInsight ai
            where ai.userId = :userId
              and ai.insightDate = :insightDate
            order by ai.createdAt desc
            """)
    List<AnalysisInsight> findTodayInsights(Long userId, LocalDate insightDate);
}