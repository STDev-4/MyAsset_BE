package io.api.myasset.global.batch.job.codefsync;

import static io.api.myasset.global.codef.constant.CodefUrl.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import io.api.myasset.domain.approval.application.ApprovalService;
import io.api.myasset.domain.approval.application.CardApprovalParser;
import io.api.myasset.domain.approval.application.SpendingCacheService;
import io.api.myasset.domain.approval.application.dto.SpendResponse;
import io.api.myasset.domain.approval.persistence.CardApproval;
import io.api.myasset.domain.approval.persistence.CardApprovalRepository;
import io.api.myasset.domain.user.entity.InstitutionType;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.batch.listener.CodefSyncJobListener;
import io.codef.api.EasyCodefClient;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefRequestBuilder;
import io.codef.api.dto.EasyCodefResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CODEF 카드 승인내역 동기화 Job 설정.
 * <p>
 * Job 은 두 Step 으로 구성:
 * <ol>
 *   <li>{@code codefSyncStep} : CODEF 호출 → {@code card_approval} 적재</li>
 *   <li>{@code spendingCacheWarmStep} : 방금 적재된 데이터로 {@link SpendResponse} 집계 → Redis 에 pre-warm</li>
 * </ol>
 * <p>
 * Step 2 의 존재 덕분에 런타임 GET /api/analysis/spending 요청은 Redis hit only 로 처리되어
 * Cache Stampede 가 발생하지 않는다 (정상 운영 기준).
 * <p>
 * 두 실행 경로 모두 동일한 두 Step 을 순차 실행한다:
 * <ul>
 *   <li>정기 배치 (스케줄러) — JobParameters 에 userId 없음 → 전체 유저</li>
 *   <li>신규 연동 (BankLinkedEvent) — JobParameters 에 userId 있음 → 해당 유저 단건.
 *       Step 2 까지 포함되므로 가입 직후 첫 요청부터 cache hit 가능.</li>
 * </ul>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CodefSyncJobConfig {

	public static final String JOB_NAME = "codefSyncJob";
	public static final String STEP_SYNC_NAME = "codefSyncStep";
	public static final String STEP_WARM_NAME = "spendingCacheWarmStep";
	public static final String PARAM_USER_ID = "userId";
	public static final String PARAM_RUN_AT = "runAt";

	private static final int CHUNK_SIZE = 20;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final UserRepository userRepository;
	private final CardApprovalRepository cardApprovalRepository;
	private final CardApprovalParser cardApprovalParser;
	private final EasyCodefClient codef;
	private final ApprovalService approvalService;
	private final SpendingCacheService cacheService;

	@Bean(JOB_NAME)
	public Job codefSyncJob(
		JobRepository jobRepository,
		Step codefSyncStep,
		Step spendingCacheWarmStep,
		CodefSyncJobListener jobListener) {
		return new JobBuilder(JOB_NAME, jobRepository)
			.listener(jobListener)
			.start(codefSyncStep)
			.next(spendingCacheWarmStep)
			.build();
	}

	// ─────────────────────────────────────────────────────────
	// Step 1. CODEF → card_approval
	// ─────────────────────────────────────────────────────────

	@Bean
	public Step codefSyncStep(
		JobRepository jobRepository,
		PlatformTransactionManager txManager,
		ItemReader<User> userReader) {
		return new StepBuilder(STEP_SYNC_NAME, jobRepository)
			.<User, List<CardApproval>>chunk(CHUNK_SIZE)
			.reader(userReader)
			.processor(fetchProcessor())
			.writer(approvalWriter())
			.transactionManager(txManager)
			.faultTolerant()
			.skip(Exception.class)
			.skipLimit(Integer.MAX_VALUE)
			.build();
	}

	/**
	 * JobParameters 의 userId 유무로 단건/전체를 분기.
	 * <p>
	 * {@code @StepScope} 로 선언하여 Step 1, Step 2 가 각자 새 인스턴스를 받는다
	 * (ListItemReader 가 stateful 이므로 @JobScope 로 공유하면 Step 2 가 빈 리스트를 받게 됨).
	 */
	@Bean
	@StepScope
	public ItemReader<User> userReader(
		@Value("#{jobParameters['" + PARAM_USER_ID + "']}")
		Long userId) {
		List<User> users;
		if (userId != null) {
			users = userRepository.findByIdWithInstitutions(userId)
				.map(List::of)
				.orElseGet(Collections::emptyList);
			log.info("[CodefSyncJob] 단건 실행 - userId={}, found={}", userId, !users.isEmpty());
		} else {
			users = userRepository.findAllWithConnectedIdAndInstitutions();
			log.info("[CodefSyncJob] 전체 실행 - targetUsers={}", users.size());
		}
		return new ListItemReader<>(new ArrayList<>(users));
	}

	private ItemProcessor<User, List<CardApproval>> fetchProcessor() {
		return user -> {
			String connectedId = user.getConnectedId();
			List<InstitutionType> cardInstitutions = user.getCardInstitutions();

			if (connectedId == null || cardInstitutions.isEmpty()) {
				log.warn(
					"[CodefSyncJob] 건너뜀 - userId={}, connectedId={}, 카드기관수={} "
						+ "(card 가 user_linked_institutions 에 존재하는지 확인 필요)",
					user.getId(), connectedId, cardInstitutions.size());
				return Collections.emptyList();
			}

			DateRange range = computeDateRange();

			List<CardApproval> all = new ArrayList<>();
			for (InstitutionType institution : cardInstitutions) {
				try {
					EasyCodefResponse response = requestApproval(
						connectedId, institution, range.startDate(), range.endDate());
					all.addAll(cardApprovalParser.parse(response, connectedId));
				} catch (Exception e) {
					log.warn("[CodefSyncJob] 기관 호출 실패 - userId={}, institution={}, reason={}",
						user.getId(), institution.getDisplayName(), e.getMessage());
				}
			}
			log.info("[CodefSyncJob] 수집 완료 - userId={}, approvals={}",
				user.getId(), all.size());
			return all;
		};
	}

	/**
	 * chunk 내 여러 유저의 List<CardApproval> 을 flatten 하여 저장한다.
	 * <p>
	 * UNIQUE 제약 위반 예외를 catch 해도 Spring 트랜잭션은 이미 rollback-only 로 마킹되어
	 * 최종 commit 시 UnexpectedRollbackException 이 발생한다. 따라서 DB 에 부딪히기 전에
	 * SELECT 로 기존 키를 조회해 메모리에서 선제적으로 중복을 제거한다.
	 */
	private ItemWriter<List<CardApproval>> approvalWriter() {
		return chunk -> {
			List<CardApproval> flat = chunk.getItems().stream()
				.flatMap(List::stream)
				.toList();

			if (flat.isEmpty())
				return;

			List<CardApproval> toInsert = filterOutExisting(flat);

			int dupCount = flat.size() - toInsert.size();
			if (toInsert.isEmpty()) {
				log.info("[CodefSyncJob] 신규 저장 없음 - 전체 {}건 모두 중복", flat.size());
				return;
			}

			cardApprovalRepository.saveAll(toInsert);
			log.info("[CodefSyncJob] 저장 완료 - 신규 {}건, 중복 스킵 {}건",
				toInsert.size(), dupCount);
		};
	}

	private List<CardApproval> filterOutExisting(List<CardApproval> candidates) {
		Map<String, List<CardApproval>> byConnectedId = candidates.stream()
			.collect(Collectors.groupingBy(CardApproval::getConnectedId));

		List<CardApproval> result = new ArrayList<>();
		for (Map.Entry<String, List<CardApproval>> entry : byConnectedId.entrySet()) {
			String connectedId = entry.getKey();
			List<CardApproval> group = entry.getValue();

			String minDate = group.stream()
				.map(CardApproval::getApprovalDate)
				.min(String::compareTo)
				.orElseThrow();
			String maxDate = group.stream()
				.map(CardApproval::getApprovalDate)
				.max(String::compareTo)
				.orElseThrow();

			List<CardApproval> existing = cardApprovalRepository
				.findByConnectedIdAndApprovalDateBetween(connectedId, minDate, maxDate);

			Set<DedupKey> existingKeys = new HashSet<>();
			for (CardApproval a : existing) {
				existingKeys.add(DedupKey.of(a));
			}

			Set<DedupKey> seenInChunk = new HashSet<>();
			for (CardApproval a : group) {
				DedupKey key = DedupKey.of(a);
				if (existingKeys.contains(key) || !seenInChunk.add(key)) {
					continue;
				}
				result.add(a);
			}
		}
		return result;
	}

	/**
	 * UNIQUE 제약과 동일한 4개 컬럼으로 구성된 중복 판정 키.
	 * Record 의 auto-generated equals/hashCode 로 안전하게 Set 사용 가능.
	 */
	private record DedupKey(
		String connectedId,
		String approvalDate,
		String merchantType,
		long approvalAmount) {
		static DedupKey of(CardApproval a) {
			return new DedupKey(
				a.getConnectedId(),
				a.getApprovalDate(),
				a.getMerchantType(),
				a.getApprovalAmount());
		}
	}

	// ─────────────────────────────────────────────────────────
	// Step 2. card_approval → Redis pre-warm (지난달 + 이번달 둘 다 집계)
	// ─────────────────────────────────────────────────────────

	/**
	 * Step 1 이 적재한 데이터를 기반으로 유저별 {@link SpendResponse} 를 집계해 Redis 에 기록한다.
	 * <p>
	 * <b>지난달 + 이번달 동시 세팅</b>:
	 * <ul>
	 *   <li>{@code last_month} key : 월말 기준 전체 데이터 — FE 의 기본 조회 대상</li>
	 *   <li>{@code this_month} key : 오늘까지 부분 데이터 — 월 경계 (예: 5/1 00:00~04:00) 에서
	 *       "지난달" 의 의미가 바뀌어도 cache hit 을 유지하기 위함</li>
	 * </ul>
	 * <p>
	 * - 캐시 우회 경로 ({@link ApprovalService#computeSpendingFromDb}) 사용 → 어제자 stale cache 영향 없음
	 * - chunk 실패 시 skip (일부 유저 warm 실패해도 Job 자체는 완료)
	 */
	@Bean
	public Step spendingCacheWarmStep(
		JobRepository jobRepository,
		PlatformTransactionManager txManager,
		ItemReader<User> userReader) {
		return new StepBuilder(STEP_WARM_NAME, jobRepository)
			.<User, List<CacheEntry>>chunk(CHUNK_SIZE)
			.reader(userReader)
			.processor(cacheWarmProcessor())
			.writer(cacheWarmWriter())
			.transactionManager(txManager)
			.faultTolerant()
			.skip(Exception.class)
			.skipLimit(Integer.MAX_VALUE)
			.build();
	}

	private ItemProcessor<User, List<CacheEntry>> cacheWarmProcessor() {
		return user -> {
			if (user.getConnectedId() == null) {
				return null;
			}

			YearMonth lastMonth = YearMonth.now().minusMonths(1);
			LocalDate today = LocalDate.now();

			String lastEndDate = lastMonth.atEndOfMonth().format(DATE_FORMAT);
			String thisEndDate = today.format(DATE_FORMAT);

			SpendResponse lastResp = approvalService.computeSpendingFromDb(user.getId(), lastEndDate);
			SpendResponse thisResp = approvalService.computeSpendingFromDb(user.getId(), thisEndDate);

			return List.of(
				new CacheEntry(user.getId(), SpendingCacheService.toYearMonth(lastEndDate), lastResp),
				new CacheEntry(user.getId(), SpendingCacheService.toYearMonth(thisEndDate), thisResp));
		};
	}

	private ItemWriter<List<CacheEntry>> cacheWarmWriter() {
		return chunk -> {
			int total = 0;
			for (List<CacheEntry> entries : chunk.getItems()) {
				for (CacheEntry entry : entries) {
					cacheService.put(entry.userId(), entry.yearMonth(), entry.response());
					total++;
				}
			}
			log.info("[CodefSyncJob] 캐시 warm 완료 - {}건 (유저 {}명 × 지난달·이번달)",
				total, chunk.getItems().size());
		};
	}

	private record CacheEntry(Long userId, String yearMonth, SpendResponse response) {
	}

	// ─────────────────────────────────────────────────────────
	// 공용 유틸
	// ─────────────────────────────────────────────────────────

	private EasyCodefResponse requestApproval(
		String connectedId, InstitutionType institution, String startDate, String endDate) {
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("organization", institution.getCodefOrgCode());
		parameter.put("connectedId", connectedId);
		parameter.put("startDate", startDate);
		parameter.put("endDate", endDate);
		parameter.put("orderBy", "0");
		parameter.put("inquiryType", "1");
		parameter.put("memberStoreInfoType", "1");

		EasyCodefRequest request = EasyCodefRequestBuilder.builder()
			.productUrl(APPROVAL_URL)
			.parameterMap(parameter)
			.build();

		return codef.requestProduct(request);
	}

	/**
	 * 조회 범위: 지난달 1일 ~ 오늘.
	 * <p>
	 * - 지난달 데이터: 소비 패턴 분석 (AnalysisPage 도넛/카테고리)
	 * - 이번달 데이터(오늘까지): 미션 성공/실패 판정 (MissionsPage 자정 판정)
	 * <p>
	 * 예) 오늘이 2026-04-17 이면 → startDate=20260301, endDate=20260417
	 */
	private DateRange computeDateRange() {
		LocalDate today = LocalDate.now();
		String endDate = today.format(DATE_FORMAT);
		String startDate = YearMonth.from(today)
			.minusMonths(1)
			.atDay(1)
			.format(DATE_FORMAT);
		log.debug("[CodefSyncJob] 조회 범위 - {} ~ {}", startDate, endDate);
		return new DateRange(startDate, endDate);
	}

	private record DateRange(String startDate, String endDate) {
	}
}
