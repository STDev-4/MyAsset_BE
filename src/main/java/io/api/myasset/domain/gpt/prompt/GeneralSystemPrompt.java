package io.api.myasset.domain.gpt.prompt;

public class GeneralSystemPrompt {

	private final PromptTemplate systemPrompt = new SystemPrompt();

	public PromptTemplate compose(PromptTemplate domainPrompt, PromptTemplate dataPrompt) {
		return () -> {
			StringBuilder sb = new StringBuilder();

			// 기본 system prompt, 항상 포함
			sb.append(systemPrompt.render());

			// 도메인 프롬프트
			if (domainPrompt != null) {
				sb.append("\n\n").append(domainPrompt.render());
			}

			// 필요 데이터
			if (dataPrompt != null) {
				sb.append("\n\n").append(dataPrompt.render());
			}
			return sb.toString();
		};
	}
}
