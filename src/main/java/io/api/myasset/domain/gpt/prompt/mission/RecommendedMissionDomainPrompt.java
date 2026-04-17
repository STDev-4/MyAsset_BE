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
                      "rewardPoint": 15,
                      "expectedSavingAmount": 24000,
                      "behaviorInsights": ["행동과학 설명1", "행동과학 설명2"],
                      "statisticalReasons": ["통계 설명1", "통계 설명2"]
                    }
                  ]
                }

                규칙:
                - 반드시 JSON만 반환한다.
                - 설명 문장, 마크다운, 코드블록을 절대 포함하지 않는다.
                - 반드시 한국어로 작성한다.
                - missions는 2~3개 생성한다.
                - title은 사용자가 바로 이해할 수 있는 실천형 문장으로 작성한다.
                - title은 반드시 오늘 바로 실행 가능한 행동 문장으로 작성한다.
                - description은 짧고 명확하게 작성한다.
                - description은 실제 상위 소비 업종과 자연스럽게 연결되도록 작성한다.
                - iconType은 DELIVERY, CAFE, SHOPPING, TRANSPORT, FOOD, SAVING 중 하나만 사용한다.
                - iconType은 미션의 대상 소비 업종과 가장 잘 맞는 값으로 선택한다.
                - rewardPoint는 10 이상 20 이하의 정수로 작성한다.
                - expectedSavingAmount는 원 단위 정수로 작성한다.
                - expectedSavingAmount는 해당 미션을 하루 실천했을 때 현실적으로 아낄 수 있는 금액으로 작성한다.
                - behaviorInsights는 2~3개 작성한다.
                - behaviorInsights는 왜 해당 소비가 반복되는지 행동과학적으로 설명한다.
                - statisticalReasons는 2~3개 작성한다.
                - statisticalReasons는 해당 미션을 했을 때 기대되는 절약 효과를 설명한다.
                - 오늘 수행 가능한 미션만 제안한다.
                - 지나치게 극단적이거나 불가능한 목표는 금지한다.
                - 반드시 사용자 소비 데이터에 포함된 상위 업종을 우선 반영한다.
                - 상위 업종과 직접 관련된 미션을 생성한다.
                - 가장 지출이 큰 업종을 우선 대상으로 삼아 미션을 제안한다.
                - 일반적인 절약 조언만 반복하지 않는다.
                - 각 미션은 서로 다른 소비 업종 또는 다른 행동 전략을 반영하도록 작성한다.
                - 동일한 의미의 미션을 표현만 바꿔 중복 생성하지 않는다.
                """;
    }
}