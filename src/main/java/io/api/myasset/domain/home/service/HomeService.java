package io.api.myasset.domain.home.service;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.character.entity.UserCharacter;
import io.api.myasset.domain.home.dto.response.HomeRankingPercentileResponse;
import io.api.myasset.domain.home.dto.response.HomeSummaryResponse;
import io.api.myasset.domain.mission.enums.MissionStatus;
import io.api.myasset.domain.mission.repository.MissionRepository;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

	private final UserRepository userRepository;
	private final MissionRepository missionRepository;

	// 홈 요약 정보 조회
	public HomeSummaryResponse getHomeSummary(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		LocalDate today = LocalDate.now();

		int totalMissionCount = missionRepository.countByUserIdAndMissionDate(userId, today);
		int completedMissionCount = missionRepository.countByUserIdAndMissionDateAndStatus(
			userId,
			today,
			MissionStatus.COMPLETED);

		int successRate = calculateSuccessRate(totalMissionCount, completedMissionCount);
		String currentImageName = extractCurrentImageName(user);
		String currentImageUrl = extractCurrentImageUrl(user);

		return HomeSummaryResponse.of(
			currentImageName,
			user.getTier().name(),
			user.getPoint(),
			currentImageUrl,
			totalMissionCount,
			completedMissionCount,
			successRate);
	}

	// 성공률 계산
	private int calculateSuccessRate(int totalMissionCount, int completedMissionCount) {
		if (totalMissionCount == 0) {
			return 0;
		}

		return (int)Math.round((double)completedMissionCount * 100 / totalMissionCount);
	}

	// 현재 활성화된 캐릭터 이름 조회
	private String extractCurrentImageName(User user) {
		return user.getUserCharacters().stream()
			.filter(UserCharacter::isActive)
			.findFirst()
			.map(userCharacter -> userCharacter.getCharacter().getName())
			.orElse(null);
	}

	// 현재 활성화된 캐릭터 이미지 URL 조회
	private String extractCurrentImageUrl(User user) {
		return user.getUserCharacters().stream()
			.filter(UserCharacter::isActive)
			.findFirst()
			.map(userCharacter -> userCharacter.getCharacter().getImageUrl())
			.orElse(null);
	}

	// 절약력 상위 퍼센트 조회
	public HomeRankingPercentileResponse getRankingPercentile(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		Integer myPoint = user.getPoint();
		long totalUserCount = userRepository.count();

		if (totalUserCount == 0) {
			return HomeRankingPercentileResponse.builder()
				.rankingPercentile("0.0")
				.measuredAt(LocalDate.now())
				.build();
		}

		long higherUserCount = userRepository.countByPointGreaterThan(myPoint);
		long rank = higherUserCount + 1;

		double calculatedPercentile = (((double)rank - 0.5) * 100) / totalUserCount;
		calculatedPercentile = Math.round(calculatedPercentile * 10) / 10.0;

		String rankingPercentile = String.format(Locale.US, "%.1f", calculatedPercentile);

		return HomeRankingPercentileResponse.builder()
			.rankingPercentile(rankingPercentile)
			.measuredAt(LocalDate.now())
			.build();
	}
}
