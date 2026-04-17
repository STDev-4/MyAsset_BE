package io.api.myasset.domain.mission.repository;

import io.api.myasset.domain.mission.entity.Mission;
import io.api.myasset.domain.mission.enums.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    @Query("""
            select m
            from Mission m
            where m.userId = :userId
              and m.missionDate = :missionDate
            order by m.createdAt asc
            """)
    List<Mission> findTodayMissions(Long userId, LocalDate missionDate);

    Optional<Mission> findByIdAndUserId(Long id, Long userId);

    @Query("""
            select case when count(m) > 0 then true else false end
            from Mission m
            where m.userId = :userId
              and m.recommendationId = :recommendationId
            """)
    boolean existsAcceptedMission(Long userId, String recommendationId);

    List<Mission> findByStatusAndAutoEvaluateAtLessThanEqual(MissionStatus status, LocalDateTime time);

    int countByUserIdAndMissionDate(Long userId, LocalDate missionDate);

    int countByUserIdAndMissionDateAndStatus(Long userId, LocalDate missionDate, MissionStatus status);
}