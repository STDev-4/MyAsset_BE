package io.api.myasset.domain.analysisinsight.service;

import io.api.myasset.domain.analysisinsight.converter.JsonListConverter;
import io.api.myasset.domain.analysisinsight.dto.AnalysisInsightItemResponse;
import io.api.myasset.domain.analysisinsight.dto.gpt.GptInsightResponse;
import io.api.myasset.domain.analysisinsight.entity.AnalysisInsight;
import io.api.myasset.domain.analysisinsight.repository.AnalysisInsightRepository;
import io.api.myasset.domain.gpt.executor.GptExecutor;
import io.api.myasset.domain.gpt.prompt.DataPrompt;
import io.api.myasset.domain.gpt.prompt.PromptTemplate;
import io.api.myasset.domain.gpt.prompt.analysis.AnalysisInsightDomainPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisInsightService {

    private final AnalysisInsightRepository analysisInsightRepository;
    private final JsonListConverter jsonListConverter;
    private final GptExecutor gptExecutor;
    private final AnalysisInsightDomainPrompt analysisInsightDomainPrompt;
    private final AnalysisInsightCacheService analysisInsightCacheService;

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

        PromptTemplate dataPrompt = new DataPrompt(
                "사용자 소비 데이터",
                """
                - 최근 소비에서 식비, 배달, 카페 비중이 높다.
                - 저녁 시간대 소액 결제가 반복된다.
                - 간편결제 사용 비중이 높아 결제 마찰이 낮다.
                - 충동 소비를 줄이기 위한 개입이 필요하다.
                """
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
}