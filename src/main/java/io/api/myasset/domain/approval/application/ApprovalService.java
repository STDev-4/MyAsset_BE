package io.api.myasset.domain.approval.application;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.approval.application.dto.SpendResponse;
import io.api.myasset.domain.approval.application.dto.SpendingTopResponse;
import io.api.myasset.domain.approval.persistence.CardApproval;
import io.api.myasset.domain.approval.persistence.CardApprovalRepository;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카드 승인내역 조회/집계 서비스.
 * <p>
 * Spring Batch 도입 이후 CODEF 호출은 {@code CodefSyncJob} 이 전담하며,
 * 이 서비스는 DB 에 적재된 데이터를 읽어 집계하는 역할만 수행한다.
 * 런타임 요청이 CODEF 호출을 유발하지 않도록 fetch 분기는 제거되었다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalService {

	private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
	private static final DateTimeFormatter YEAR_MONTH_DASH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

	private final CardApprovalRepository cardApprovalRepository;
	private final UserRepository userRepository;

	/**
	 * 특정 유저의 지정된 월까지 포함한 소비 집계를 반환한다.
	 *
	 * @param userId  유저 ID
	 * @param endDate yyyyMMdd 형식 (예: 20260331)
	 */
	public SpendResponse getSpending(Long userId, String endDate) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		String connectedId = user.getConnectedId();
		if (connectedId == null) {
			// 연동 전 유저: 빈 응답
			return emptyResponse(endDate);
		}

		YearMonth endMonth = YearMonth.parse(endDate.substring(0, 6), YEAR_MONTH_FORMAT);
		String startDate = endMonth.minusMonths(2)
			.atDay(1)
			.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		List<CardApproval> approvals = cardApprovalRepository
			.findByConnectedIdAndApprovalDateBetween(connectedId, startDate, endDate);

		return buildSpendResponse(approvals, endDate);
	}

	private SpendResponse buildSpendResponse(List<CardApproval> approvals, String endDate) {
		YearMonth currentMonth = YearMonth.parse(endDate.substring(0, 6), YEAR_MONTH_FORMAT);
		String currentPrefix = currentMonth.format(YEAR_MONTH_FORMAT);
		String previousPrefix = currentMonth.minusMonths(1).format(YEAR_MONTH_FORMAT);

		List<CardApproval> currentApprovals = approvals.stream()
			.filter(a -> a.getApprovalDate().startsWith(currentPrefix))
			.toList();

		long totalAmount = currentApprovals.stream()
			.mapToLong(CardApproval::getApprovalAmount)
			.sum();

		long previousTotalAmount = approvals.stream()
			.filter(a -> a.getApprovalDate().startsWith(previousPrefix))
			.mapToLong(CardApproval::getApprovalAmount)
			.sum();

		List<SpendResponse.CategorySpending> categories = currentApprovals.stream()
			.collect(Collectors.groupingBy(
				CardApproval::getMerchantType,
				Collectors.summingLong(CardApproval::getApprovalAmount)))
			.entrySet().stream()
			.map(e -> new SpendResponse.CategorySpending(e.getKey(), e.getValue()))
			.sorted(Comparator.comparingLong(SpendResponse.CategorySpending::amount).reversed())
			.toList();

		return new SpendResponse(
			currentMonth.format(YEAR_MONTH_DASH_FORMAT),
			totalAmount, previousTotalAmount, categories);
	}

	/**
	 * 특정 유저의 지정된 월의 Top N 소비 업종을 반환한다.
	 *
	 * @param userId  유저 ID
	 * @param endDate yyyyMMdd 형식
	 * @param limit   상위 개수 (default 3)
	 */
	public SpendingTopResponse getTopSpending(Long userId, String endDate, int limit) {
		SpendResponse spending = getSpending(userId, endDate);

		List<SpendingTopResponse.TopCategory> items = new java.util.ArrayList<>();
		int rank = 1;
		for (SpendResponse.CategorySpending c : spending.categories()) {
			if (rank > limit)
				break;
			items.add(new SpendingTopResponse.TopCategory(rank++, c.category(), c.amount()));
		}

		return new SpendingTopResponse(spending.yearMonth(), items);
	}

	private SpendResponse emptyResponse(String endDate) {
		YearMonth month = YearMonth.parse(endDate.substring(0, 6), YEAR_MONTH_FORMAT);
		return new SpendResponse(month.format(YEAR_MONTH_DASH_FORMAT), 0L, 0L, List.of());
	}
}
