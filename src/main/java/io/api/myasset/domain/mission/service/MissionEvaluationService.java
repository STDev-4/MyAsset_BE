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

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionEvaluationService {

	private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final MissionRepository missionRepository;
	private final UserRepository userRepository;
	private final CardApprovalRepository cardApprovalRepository;

	@Transactional
	public void evaluateExpiredMissions() {
		LocalDateTime now = LocalDateTime.now();

		List<Mission> targets = missionRepository.findByStatusAndAutoEvaluateAtLessThanEqual(
			MissionStatus.IN_PROGRESS,
			now);

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
			return;
		}

		String approvalDate = mission.getMissionDate().format(YYYYMMDD);

		List<CardApproval> approvals = cardApprovalRepository.findByConnectedIdAndApprovalDate(
			user.getConnectedId(),
			approvalDate);

		boolean success = judge(mission, approvals);

		if (success) {
			mission.completeSuccess();
			user.addPoint(mission.getRewardPoint());
			log.info("[MissionEvaluation] 성공 - missionId={}, userId={}, rewardPoint={}",
				mission.getId(), user.getId(), mission.getRewardPoint());
		} else {
			mission.completeFail();
			log.info("[MissionEvaluation] 실패 - missionId={}, userId={}", mission.getId(), user.getId());
		}
	}

	private boolean judge(Mission mission, List<CardApproval> approvals) {
		String title = normalize(mission.getTitle());
		String description = normalize(mission.getDescription());

		if (containsAny(title, description, "커피", "카페")) {
			return countByMerchant(approvals, "커피전문점") <= 1;
		}

		if (containsAny(title, description, "택시")) {
			return countByMerchant(approvals, "택시") == 0;
		}

		if (containsAny(title, description, "편의점")) {
			return sumByMerchant(approvals, "편의점") <= 5000;
		}

		if (containsAny(title, description, "배달", "치킨", "패스트푸드", "외식", "식비")) {
			long foodTotal = sumByKeywords(approvals,
				"일반음식점", "치킨전문점", "패스트푸드점", "일식전문점");
			return foodTotal <= mission.getExpectedSavingAmount();
		}

		// 규칙을 모르겠으면 보수적으로 실패 처리
		return false;
	}

	private String normalize(String value) {
		return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase();
	}

	private boolean containsAny(String title, String description, String... keywords) {
		for (String keyword : keywords) {
			String k = keyword.replaceAll("\\s+", "").toLowerCase();
			if (title.contains(k) || description.contains(k)) {
				return true;
			}
		}
		return false;
	}

	private long countByMerchant(List<CardApproval> approvals, String merchantType) {
		return approvals.stream()
			.filter(a -> merchantType.equals(a.getMerchantType()))
			.count();
	}

	private long sumByMerchant(List<CardApproval> approvals, String merchantType) {
		return approvals.stream()
			.filter(a -> merchantType.equals(a.getMerchantType()))
			.mapToLong(CardApproval::getApprovalAmount)
			.sum();
	}

	private long sumByKeywords(List<CardApproval> approvals, String... merchantTypes) {
		return approvals.stream()
			.filter(a -> {
				for (String merchantType : merchantTypes) {
					if (merchantType.equals(a.getMerchantType())) {
						return true;
					}
				}
				return false;
			})
			.mapToLong(CardApproval::getApprovalAmount)
			.sum();
	}
}
