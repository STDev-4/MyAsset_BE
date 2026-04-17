package io.api.myasset.domain.mission.service;

import io.api.myasset.domain.mission.dto.MissionStatusCardResponse;
import io.api.myasset.domain.mission.enums.MissionStatus;
import io.api.myasset.domain.mission.repository.MissionRepository;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MissionDashboardService {

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MissionStatusCardResponse getMissionStatusCard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        int todayTotal = missionRepository.countByUserIdAndMissionDate(userId, today);
        int todayCompleted = missionRepository.countByUserIdAndMissionDateAndStatus(
                userId, today, MissionStatus.COMPLETED
        );

        int yesterdayTotal = missionRepository.countByUserIdAndMissionDate(userId, yesterday);
        int yesterdayCompleted = missionRepository.countByUserIdAndMissionDateAndStatus(
                userId, yesterday, MissionStatus.COMPLETED
        );

        int yesterdayPercent = yesterdayTotal == 0 ? 0 : (yesterdayCompleted * 100) / yesterdayTotal;

        String remainingTime = formatRemainingTime(LocalDateTime.now(), today.atTime(23, 59, 59));

        int totalPoint = user.getPoint();
        int pointToNextTier = calculatePointToNextTier(user);

        return new MissionStatusCardResponse(
                todayCompleted,
                todayTotal,
                yesterdayPercent,
                remainingTime,
                totalPoint,
                pointToNextTier
        );
    }

    private String formatRemainingTime(LocalDateTime now, LocalDateTime target) {
        if (now.isAfter(target)) {
            return "00:00:00";
        }

        Duration duration = Duration.between(now, target);
        long seconds = duration.getSeconds();

        long hour = seconds / 3600;
        long minute = (seconds % 3600) / 60;
        long second = seconds % 60;

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    private int calculatePointToNextTier(User user) {
        Integer nextTierRequiredPoint = user.getTier().getNextTierRequiredPoint();

        if (nextTierRequiredPoint == null) {
            return 0;
        }

        return Math.max(nextTierRequiredPoint - user.getPoint(), 0);
    }
}