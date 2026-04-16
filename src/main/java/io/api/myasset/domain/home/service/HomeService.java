package io.api.myasset.domain.home.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.home.dto.response.HomeRankingPercentileResponse;
import io.api.myasset.domain.home.dto.response.HomeStreakResponse;
import io.api.myasset.domain.home.entity.SavingHistory;
import io.api.myasset.domain.home.repository.SavingHistoryRepository;
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
	private final SavingHistoryRepository savingHistoryRepository;

	// 절약력 상위 퍼센트 조회
	public HomeRankingPercentileResponse getRankingPercentile(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		Integer myPoint = user.getPoint();
		long totalUserCount = userRepository.count();

		if (totalUserCount == 0) {
			return HomeRankingPercentileResponse.builder()
				.rankingPercentile(0)
				.measuredAt(LocalDate.now())
				.build();
		}

		long higherUserCount = userRepository.countByPointGreaterThan(myPoint);
		long myRank = higherUserCount + 1;

		int rankingPercentile = (int)Math.round((double)myRank * 100 / totalUserCount);

		return HomeRankingPercentileResponse.builder()
			.rankingPercentile(rankingPercentile)
			.measuredAt(LocalDate.now())
			.build();
	}

	// 연속 절약일수 조회
	public HomeStreakResponse getStreak(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		LocalDate today = LocalDate.now();

		int streakDays = calculateStreakDays(user, today);
		List<Boolean> weekProgress = calculateWeekProgress(user, today);
		int todayIndex = convertToMondayStartIndex(today.getDayOfWeek());

		return HomeStreakResponse.builder()
			.streakDays(streakDays)
			.weekProgress(weekProgress)
			.todayIndex(todayIndex)
			.build();
	}

	// 연속 절약일수 계산
	private int calculateStreakDays(User user, LocalDate today) {
		List<SavingHistory> savingHistories = savingHistoryRepository.findByUserAndSuccessDateLessThanEqual(user,
			today);

		Set<LocalDate> successDateSet = new HashSet<>();
		for (SavingHistory savingHistory : savingHistories) {
			successDateSet.add(savingHistory.getSuccessDate());
		}

		int streakDays = 0;
		LocalDate targetDate = today;

		while (successDateSet.contains(targetDate)) {
			streakDays++;
			targetDate = targetDate.minusDays(1);
		}

		return streakDays;
	}

	// 주간 성공 현황 계산
	private List<Boolean> calculateWeekProgress(User user, LocalDate today) {
		LocalDate weekStartDate = today.minusDays(convertToMondayStartIndex(today.getDayOfWeek()));
		LocalDate weekEndDate = weekStartDate.plusDays(6);

		List<SavingHistory> savingHistories = savingHistoryRepository.findByUserAndSuccessDateBetween(user,
			weekStartDate, weekEndDate);

		Set<LocalDate> successDateSet = new HashSet<>();
		for (SavingHistory savingHistory : savingHistories) {
			successDateSet.add(savingHistory.getSuccessDate());
		}

		List<Boolean> weekProgress = new ArrayList<>();
		for (int index = 0; index < 7; index++) {
			LocalDate targetDate = weekStartDate.plusDays(index);
			weekProgress.add(successDateSet.contains(targetDate));
		}

		return weekProgress;
	}

	// 월요일 시작 인덱스로 변환
	private int convertToMondayStartIndex(DayOfWeek dayOfWeek) {
		return dayOfWeek.getValue() - 1;
	}
}
