package io.api.myasset.domain.mission.repository;

import io.api.myasset.domain.mission.entity.RecommendedMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecommendedMissionRepository extends JpaRepository<RecommendedMission, Long> {

    @Query("""
            select rm
            from RecommendedMission rm
            where rm.userId = :userId
              and rm.recommendDate = :recommendDate
              and rm.accepted = false
            order by rm.createdAt desc
            """)
    List<RecommendedMission> findTodayRecommendedMissions(Long userId, LocalDate recommendDate);

    Optional<RecommendedMission> findByIdAndUserId(Long id, Long userId);
}