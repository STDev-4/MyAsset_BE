package io.api.myasset.domain.mission.service;

import io.api.myasset.domain.approval.persistence.CardApproval;
import io.api.myasset.domain.approval.persistence.CardApprovalRepository;
import io.api.myasset.domain.mission.entity.Mission;
import io.api.myasset.domain.mission.enums.MissionStatus;
import io.api.myasset.domain.mission.repository.MissionRepository;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionEvaluationService {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Set<String> COFFEE_TYPES = Set.of(
            "커피전문점"
    );

    private static final Set<String> TAXI_TYPES = Set.of(
            "택시"
    );

    private static final Set<String> CONVENIENCE_TYPES = Set.of(
            "편의점"
    );

    private static final Set<String> FOOD_TYPES = Set.of(
            "일반음식점",
            "배달의민족",
            "패스트푸드점",
            "일식전문점",
            "치킨전문점",
            "일반주점",
            "제과 제빵",
            "아이스크림전문",
            "기타식음료품"
    );

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final CardApprovalRepository cardApprovalRepository;

    @Transactional
    public void evaluateExpiredMissions() {
        LocalDateTime now = LocalDateTime.now();

        List<Mission> targets = missionRepository.findByStatusAndAutoEvaluateAtLessThanEqual(
                MissionStatus.IN_PROGRESS,
                now
        );

        log.info("[MissionEvaluation] 자동 판정 시작 - targetCount={}", targets.size());

        for (Mission mission : targets) {
            evaluateOne(mission);
        }

        log.info("[MissionEvaluation] 자동 판정 종료");
    }

    private void evaluateOne(Mission mission) {
        User user = userRepository.findById(mission.getUserId())
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        if (user.getConnectedId() == null || user.getConnectedId().isBlank()) {
            log.warn("[MissionEvaluation] connectedId 없음 - missionId={}, userId={}", mission.getId(), user.getId());
            mission.completeFail();

            int penaltyPoint = mission.getRewardPoint() / 2;
            user.subtractPoint(penaltyPoint);

            log.info("[MissionEvaluation] 실패(connectedId 없음) - missionId={}, userId={}, penaltyPoint={}",
                    mission.getId(), user.getId(), penaltyPoint);
            return;
        }

        String approvalDate = mission.getMissionDate().format(YYYYMMDD);

        List<CardApproval> approvals = cardApprovalRepository.findByConnectedIdAndApprovalDate(
                user.getConnectedId(),
                approvalDate
        );

        boolean success = judge(mission, approvals);

        if (success) {
            mission.completeSuccess();
            user.addPoint(mission.getRewardPoint());
            log.info("[MissionEvaluation] 성공 - missionId={}, userId={}, rewardPoint={}",
                    mission.getId(), user.getId(), mission.getRewardPoint());
        } else {
            mission.completeFail();

            int penaltyPoint = mission.getRewardPoint() / 2;
            user.subtractPoint(penaltyPoint);

            log.info("[MissionEvaluation] 실패 - missionId={}, userId={}, penaltyPoint={}",
                    mission.getId(), user.getId(), penaltyPoint);
        }
    }

    private boolean judge(Mission mission, List<CardApproval> approvals) {
        String title = normalize(mission.getTitle());
        String description = normalize(mission.getDescription());

        if (containsAny(title, description, "커피", "카페")) {
            return countByTypes(approvals, COFFEE_TYPES) <= 1;
        }

        if (containsAny(title, description, "택시")) {
            return countByTypes(approvals, TAXI_TYPES) == 0;
        }

        if (containsAny(title, description, "편의점")) {
            return sumByTypes(approvals, CONVENIENCE_TYPES) <= 5000;
        }

        if (containsAny(title, description, "배달", "치킨", "패스트푸드", "외식", "식비")) {
            long foodTotal = sumByTypes(approvals, FOOD_TYPES);
            return foodTotal <= mission.getExpectedSavingAmount();
        }

        return false;
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase();
    }

    private boolean containsAny(String title, String description, String... keywords) {
        for (String keyword : keywords) {
            String k = normalize(keyword);
            if (title.contains(k) || description.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private long countByTypes(List<CardApproval> approvals, Set<String> targetTypes) {
        return approvals.stream()
                .filter(a -> matchesType(a.getMerchantType(), targetTypes))
                .count();
    }

    private long sumByTypes(List<CardApproval> approvals, Set<String> targetTypes) {
        return approvals.stream()
                .filter(a -> matchesType(a.getMerchantType(), targetTypes))
                .mapToLong(CardApproval::getApprovalAmount)
                .sum();
    }

    private boolean matchesType(String merchantType, Set<String> targetTypes) {
        String normalizedMerchantType = normalize(merchantType);
        return targetTypes.stream()
                .map(this::normalize)
                .anyMatch(normalizedMerchantType::equals);
    }
}