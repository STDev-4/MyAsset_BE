package io.api.myasset.domain.gpt.prompt.analysis;

import io.api.myasset.domain.gpt.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class AnalysisInsightDomainPrompt implements PromptTemplate {

    @Override
    public String render() {
        return """
                역할:
                사용자의 소비 습관을 심리학 및 행동과학 관점에서 해석하여
                인사이트 카드 목록을 생성한다.

                반환 형식:
                {
                  "insights": [
                    {
                      "title": "심리학 기반 인사이트 제목",
                      "description": "설명",
                      "colorType": "RED",
                      "actionTips": ["실천 방법1", "실천 방법2"]
                    }
                  ]
                }

                규칙:
                - 반드시 JSON만 반환한다.
                - 반드시 한국어로 작성한다.
                - insights는 4개 생성한다.
                - title은 반드시 심리학 또는 행동과학 개념 기반의 표현으로 작성한다.
                - title에는 소비 항목명이나 단순 행동명 대신 개념명을 사용한다.
                - title 예시: 즉시 보상 편향, 현재 편향, 기본값 효과, 손실회피 성향, 마찰비용 효과, 정신적 회계, 앵커링 효과
                - description은 1~2문장으로 작성한다.
                - description은 사용자의 소비를 비난하지 않고 이해를 돕는 말투로 작성한다.
                - colorType은 RED, BLUE, GREEN, PURPLE 중 하나만 사용한다.
                - actionTips는 반드시 2~3개 작성한다.
                - actionTips의 모든 문장은 반드시 "~하기" 형식으로 끝나야 한다.
                - actionTips는 짧고 실천 가능한 행동으로 작성한다.
                - 예시: 간편결제 해제하기, 배달앱 알림 끄기, 결제 전 10분 기다리기
                """;
    }
}