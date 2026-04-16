package io.api.myasset.domain.gpt.prompt;

public class SystemPrompt implements PromptTemplate {

    @Override
    public String render() {
        return """
                너는 개인 자산관리 서비스의 데이터 분석 AI다.
                항상 한국어로 응답해라.
                반드시 JSON 형식으로만 응답해라.
                설명 문장, 마크다운, 코드블록은 절대 포함하지 마라.
                null이 필요한 경우에만 null을 사용하고, 가능한 값은 최대한 채워라.
                """;
    }
}
