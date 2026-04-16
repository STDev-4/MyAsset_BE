package io.api.myasset.domain.home.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.api.myasset.domain.home.entity.SavingHistory;
import io.api.myasset.domain.user.entity.User;

public interface SavingHistoryRepository extends JpaRepository<SavingHistory, Long> {

	// 특정 기간의 절약 성공 기록 조회
	List<SavingHistory> findByUserAndSuccessDateBetween(User user, LocalDate startDate, LocalDate endDate);

	// 오늘까지의 절약 성공 기록 조회
	List<SavingHistory> findByUserAndSuccessDateLessThanEqual(User user, LocalDate date);

	// 특정 날짜 절약 성공 여부 조회
	boolean existsByUserAndSuccessDate(User user, LocalDate successDate);
}
