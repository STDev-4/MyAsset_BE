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
			- 설명 문장, 마크다운, 코드블록을 절대 포함하지 않는다.
			- 반드시 한국어로 작성한다.
			- insights는 정확히 4개 생성한다.
			- title은 반드시 심리학 또는 행동과학 개념 기반의 표현으로 작성한다.
			- title에는 소비 항목명이나 단순 행동명 대신 개념명을 사용한다.
			- title 예시: 즉시 보상 편향, 현재 편향, 기본값 효과, 손실회피 성향, 마찰비용 효과, 정신적 회계, 앵커링 효과
			- description은 1~2문장으로 작성한다.
			- description은 사용자의 소비를 비난하지 않고 이해를 돕는 말투로 작성한다.
			- description 안에는 반드시 실제 상위 소비 업종 중 하나 이상을 자연스럽게 반영한다.
			- colorType은 RED, BLUE, GREEN, PURPLE 중 하나만 사용한다.
			- actionTips는 반드시 2~3개 작성한다.
			- actionTips의 모든 문장은 반드시 "~하기" 형식으로 끝나야 한다.
			- actionTips는 짧고 실천 가능한 행동으로 작성한다.
			- 예시: 간편결제 해제하기, 배달앱 알림 끄기, 결제 전 10분 기다리기
			- 반드시 사용자 소비 데이터에 포함된 상위 업종을 기준으로 인사이트를 생성한다.
			- 상위 업종과 무관한 일반적인 소비 조언만 반복하지 않는다.
			- 가장 지출이 큰 업종의 반복 소비 원인을 행동과학적으로 설명한다.
			- actionTips는 해당 상위 소비 업종을 줄이기 위한 구체적인 행동이어야 한다.
			- 같은 개념을 제목만 바꿔 중복해서 쓰지 않는다.
			- 4개의 인사이트는 서로 다른 행동과학 개념으로 작성한다.
			""";
	}
}
