package io.api.myasset.domain.approval.presentation;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.domain.approval.application.ApprovalService;
import io.api.myasset.domain.approval.application.dto.SpendResponse;
import io.api.myasset.domain.approval.application.dto.SpendingTopResponse;
import io.api.myasset.global.auth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;

/**
 * 소비 데이터 조회 컨트롤러.
 * <p>
 * CodefSyncJob 이 적재한 {@code card_approval} 데이터를 읽어 FE 로 제공한다.
 * 런타임에 CODEF 를 호출하지 않으므로 응답 시간이 DB 조회 수준으로 일정하다.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SpendingController {

	private static final DateTimeFormatter YEAR_MONTH_INPUT = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final ApprovalService approvalService;

	/**
	 * 카테고리/가맹점별 소비 분석.
	 * <p>
	 * AnalysisPage 도넛 차트 데이터 소스.
	 *
	 * @param yearMonth "YYYY-MM" 형식, 미지정 시 지난달
	 */
	@GetMapping("/analysis/spending")
	public ResponseEntity<SpendResponse> getSpending(
		@RequestParam(required = false)
		String yearMonth) {
		Long userId = SecurityUtil.getCurrentUserId();
		String endDate = resolveEndDate(yearMonth);
		return ResponseEntity.ok(approvalService.getSpending(userId, endDate));
	}

	/**
	 * 지난달 Top N 소비 가맹점.
	 * <p>
	 * HomePage 지난달 Top3 카드 데이터 소스.
	 *
	 * @param limit     상위 개수 (default 3)
	 * @param yearMonth "YYYY-MM" 형식, 미지정 시 지난달
	 */
	@GetMapping("/spending/top")
	public ResponseEntity<SpendingTopResponse> getTopSpending(
		@RequestParam(defaultValue = "3")
		int limit,
		@RequestParam(required = false)
		String yearMonth) {
		Long userId = SecurityUtil.getCurrentUserId();
		String endDate = resolveEndDate(yearMonth);
		return ResponseEntity.ok(approvalService.getTopSpending(userId, endDate, limit));
	}

	/**
	 * "2026-03" → "20260331" 변환. 미지정 시 지난달 말일.
	 */
	private String resolveEndDate(String yearMonth) {
		YearMonth ym = (yearMonth == null || yearMonth.isBlank())
			? YearMonth.now().minusMonths(1)
			: YearMonth.parse(yearMonth, YEAR_MONTH_INPUT);
		return ym.atEndOfMonth().format(YYYYMMDD);
	}
}
