package io.api.myasset.domain.approval.application;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
 * <p>
 * Redis 캐시 적용 (cache-aside):
 * - {@link #getSpending(Long, String)} : cache → miss 시 DB + cache write
 * - {@link #computeSpendingFromDb(Long, String)} : Batch 가 pre-warm 시 사용 (cache 우회)
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
	private final SpendingCacheService cacheService;

	/**
	 * 특정 유저의 지정된 월까지 포함한 소비 집계를 반환한다.
	 * 캐시 우선 조회, miss 시 DB 에서 계산 후 캐시에 적재.
	 *
	 * @param userId  유저 ID
	 * @param endDate yyyyMMdd 형식 (예: 20260331)
	 */
	public SpendResponse getSpending(Long userId, String endDate) {
		String yearMonth = SpendingCacheService.toYearMonth(endDate);

		Optional<SpendResponse> cached = cacheService.get(userId, yearMonth);
		if (cached.isPresent()) {
			return cached.get();
		}

		SpendResponse result = computeSpendingFromDb(userId, endDate);
		cacheService.put(userId, yearMonth, result);
		return result;
	}

	/**
	 * DB 에서 직접 집계한 결과를 반환한다 (캐시 우회).
	 * Batch 의 pre-warming Step 이 어제 캐시를 덮어쓰기 위해 강제 재계산할 때 사용한다.
	 */
	public SpendResponse computeSpendingFromDb(Long userId, String endDate) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		String connectedId = user.getConnectedId();
		if (connectedId == null) {
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
	 * 내부적으로 {@link #getSpending(Long, String)} 호출 → 캐시 hit 시 Redis 에서 읽힌 값을 가공만 함.
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
