package io.api.myasset.domain.analysisinsight.service;

import io.api.myasset.domain.analysisinsight.converter.JsonListConverter;
import io.api.myasset.domain.analysisinsight.dto.AnalysisInsightItemResponse;
import io.api.myasset.domain.analysisinsight.dto.gpt.GptInsightResponse;
import io.api.myasset.domain.analysisinsight.entity.AnalysisInsight;
import io.api.myasset.domain.analysisinsight.repository.AnalysisInsightRepository;
import io.api.myasset.domain.approval.application.ApprovalService;
import io.api.myasset.domain.approval.application.dto.SpendingTopResponse;
import io.api.myasset.domain.gpt.executor.GptExecutor;
import io.api.myasset.domain.gpt.prompt.DataPrompt;
import io.api.myasset.domain.gpt.prompt.PromptTemplate;
import io.api.myasset.domain.gpt.prompt.analysis.AnalysisInsightDomainPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisInsightService {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AnalysisInsightRepository analysisInsightRepository;
    private final JsonListConverter jsonListConverter;
    private final GptExecutor gptExecutor;
    private final AnalysisInsightDomainPrompt analysisInsightDomainPrompt;
    private final AnalysisInsightCacheService analysisInsightCacheService;
    private final ApprovalService approvalService;

    @Transactional
    public List<AnalysisInsightItemResponse> getInsights(Long userId) {
        LocalDate today = LocalDate.now();

        List<AnalysisInsightItemResponse> cached = analysisInsightCacheService.getInsights(userId, today);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        List<AnalysisInsight> savedInsights = analysisInsightRepository.findTodayInsights(userId, today);
        if (!savedInsights.isEmpty()) {
            List<AnalysisInsightItemResponse> responses = savedInsights.stream()
                    .map(insight -> new AnalysisInsightItemResponse(
                            insight.getId(),
                            insight.getTitle(),
                            insight.getDescription(),
                            insight.getColorType(),
                            jsonListConverter.toList(insight.getActionTipsJson())
                    ))
                    .toList();

            analysisInsightCacheService.saveInsights(userId, today, responses);
            return responses;
        }

        String endDate = YearMonth.now()
                .minusMonths(1)
                .atEndOfMonth()
                .format(YYYYMMDD);

        SpendingTopResponse topSpending = approvalService.getTopSpending(userId, endDate, 3);

        String spendingData = buildInsightDataPrompt(topSpending);

        PromptTemplate dataPrompt = new DataPrompt(
                "사용자 소비 데이터",
                spendingData
        );

        GptInsightResponse gptResponse = gptExecutor.execute(
                analysisInsightDomainPrompt,
                dataPrompt,
                500,
                GptInsightResponse.class
        );

        List<AnalysisInsight> insights = gptResponse.insights().stream()
                .map(item -> AnalysisInsight.of(
                        userId,
                        item.title(),
                        item.description(),
                        item.colorType(),
                        jsonListConverter.toJson(item.actionTips()),
                        today
                ))
                .toList();

        List<AnalysisInsight> saved = analysisInsightRepository.saveAll(insights);

        List<AnalysisInsightItemResponse> responses = saved.stream()
                .map(insight -> new AnalysisInsightItemResponse(
                        insight.getId(),
                        insight.getTitle(),
                        insight.getDescription(),
                        insight.getColorType(),
                        jsonListConverter.toList(insight.getActionTipsJson())
                ))
                .toList();

        analysisInsightCacheService.saveInsights(userId, today, responses);
        return responses;
    }

    private String buildInsightDataPrompt(SpendingTopResponse topSpending) {
        if (topSpending == null || topSpending.items() == null || topSpending.items().isEmpty()) {
            return """
                    - 지난달 소비 데이터가 충분하지 않다.
                    - 식비, 카페, 쇼핑, 교통 등 일상 소비에서 반복되기 쉬운 패턴을 중심으로 인사이트를 생성한다.
                    - 충동 소비, 보상 심리, 결제 마찰 감소 같은 행동 요인을 바탕으로 분석한다.
                    - 사용자가 바로 적용할 수 있는 현실적인 action tip을 제안한다.
                    """;
        }

        String top3Text = topSpending.items().stream()
                .map(item -> "- " + item.rank() + "위: " + item.category() + " (" + item.amount() + "원)")
                .reduce((a, b) -> a + "\n" + b)
                .orElse("- 소비 데이터 없음");

        return """
                - 사용자의 지난달 소비 상위 업종은 다음과 같다.
                %s
                - 반드시 위 상위 소비 업종을 중심으로 소비 패턴을 해석한다.
                - 가장 지출이 큰 업종의 반복 원인을 우선 분석한다.
                - 인사이트는 실제 소비 업종과 직접 연결되어야 한다.
                - action tip은 해당 소비 업종을 줄이기 위해 바로 실천 가능한 행동으로 제안한다.
                - 일반적인 절약 조언보다 사용자의 실제 상위 소비 업종에 맞춘 구체적인 분석을 우선한다.
                """.formatted(top3Text);
    }
}