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

import org.springframework.batch.core.configuration.annotation.JobScope;
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

import io.api.myasset.domain.approval.application.CardApprovalParser;
import io.api.myasset.domain.approval.persistence.CardApproval;
import io.api.myasset.domain.approval.persistence.CardApprovalRepository;
import io.api.myasset.domain.user.entity.InstitutionType;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.repository.UserRepository;
import io.codef.api.EasyCodefClient;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefRequestBuilder;
import io.codef.api.dto.EasyCodefResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CODEF 카드 승인내역 동기화 Job 설정.
 * <p>
 * - JobParameters "userId" 존재 시: 단건 처리 (신규 유저 연동 직후 트리거 경로)
 * - JobParameters "userId" 없음: connectedId 를 가진 전체 유저 처리 (스케줄러 경로)
 * <p>
 * Step 은 chunk 지향 처리로, 유저 단위로 CODEF 호출 → CardApproval 리스트 생성 → BULK INSERT.
 * UNIQUE 제약으로 인한 중복 예외는 경고 로그 후 무시한다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CodefSyncJobConfig {

	public static final String JOB_NAME = "codefSyncJob";
	public static final String STEP_NAME = "codefSyncStep";
	public static final String PARAM_USER_ID = "userId";
	public static final String PARAM_RUN_AT = "runAt";

	private static final int CHUNK_SIZE = 20;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final UserRepository userRepository;
	private final CardApprovalRepository cardApprovalRepository;
	private final CardApprovalParser cardApprovalParser;
	private final EasyCodefClient codef;

	@Bean(JOB_NAME)
	public Job codefSyncJob(JobRepository jobRepository, Step codefSyncStep) {
		return new JobBuilder(JOB_NAME, jobRepository)
			.start(codefSyncStep)
			.build();
	}

	/**
	 * {@link ItemReader} 는 {@code @JobScope} 프록시이므로 {@code userReader(null)} 로
	 * 직접 호출하지 않고 Spring 주입을 받아야 한다.
	 * <p>
	 * Spring Batch 6 에서 {@code chunk(int, PlatformTransactionManager)} 가 deprecated 되어
	 * {@code chunk(int)} + {@code transactionManager(...)} 체인으로 변경되었다.
	 */
	@Bean
	public Step codefSyncStep(
		JobRepository jobRepository,
		PlatformTransactionManager txManager,
		ItemReader<User> userReader) {
		return new StepBuilder(STEP_NAME, jobRepository)
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
	 * JobParameters 의 userId 유무로 단건/전체를 분기한다.
	 * JobScope 로 선언돼 매 Job 실행 시점에 파라미터를 주입받는다.
	 */
	@Bean
	@JobScope
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

	/**
	 * 유저 1명 → 보유 카드 기관별 CODEF 호출 → CardApproval 리스트.
	 * 기관별 호출 실패는 로그만 남기고 다른 기관으로 진행한다 (부분 성공 허용).
	 */
	private ItemProcessor<User, List<CardApproval>> fetchProcessor() {
		return user -> {
			String connectedId = user.getConnectedId();
			List<InstitutionType> cardInstitutions = user.getCardInstitutions();

			if (connectedId == null || cardInstitutions.isEmpty()) {
				log.debug("[CodefSyncJob] 건너뜀 - userId={}, connectedId 없음 또는 카드 기관 없음",
					user.getId());
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

	/**
	 * connectedId 별로 {@code [minDate, maxDate]} 범위의 기존 레코드를 한 번에 조회해
	 * UNIQUE 키 조합 Set 을 만든 뒤, 들어온 리스트에서 해당 키와 겹치지 않는 것만 걸러낸다.
	 */
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
