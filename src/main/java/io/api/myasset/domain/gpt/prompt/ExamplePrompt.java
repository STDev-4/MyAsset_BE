package io.api.myasset.domain.gpt.prompt;

public class ExamplePrompt implements PromptTemplate {

	@Override
	public String render() {
		return """
			아래 조건에 맞는 테스트 응답을 생성해라.
			출력은 반드시 JSON 형식으로만 한다.
			설명 문장이나 추가 텍스트는 포함하지 마라.
			{
			  "status": "ok",
			  "message": "GPT 연결 테스트 성공"
			}
			        """;
	}
}
