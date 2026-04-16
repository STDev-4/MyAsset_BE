package io.api.myasset.domain.gpt.prompt.mission;

import io.api.myasset.domain.gpt.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class RecommendedMissionDomainPrompt implements PromptTemplate {

    @Override
    public String render() {
        return """
                역할:
                사용자의 소비 습관을 바탕으로 오늘 실천 가능한 절약 미션을 추천한다.

                반환 형식:
                {
                  "missions": [
                    {
                      "title": "미션 제목",
                      "description": "미션 설명",
                      "iconType": "DELIVERY",
                      "rewardPoint": 30,
                      "expectedSavingAmount": 24000,
                      "behaviorInsights": ["행동과학 설명1", "행동과학 설명2"],
                      "statisticalReasons": ["통계 설명1", "통계 설명2"]
                    }
                  ]
                }

                규칙:
                - 반드시 JSON만 반환한다.
                - 반드시 한국어로 작성한다.
                - missions는 2~3개 생성한다.
                - title은 사용자가 바로 이해할 수 있는 실천형 문장으로 작성한다.
                - description은 짧고 명확하게 작성한다.
                - iconType은 DELIVERY, CAFE, SHOPPING, TRANSPORT, FOOD, SAVING 중 하나만 사용한다.
                - rewardPoint는 10 이상 50 이하의 정수로 작성한다.
                - expectedSavingAmount는 원 단위 정수로 작성한다.
                - behaviorInsights는 2~3개 작성한다.
                - statisticalReasons는 2~3개 작성한다.
                - 오늘 수행 가능한 미션만 제안한다.
                - 지나치게 극단적이거나 불가능한 목표는 금지한다.
                """;
    }
}