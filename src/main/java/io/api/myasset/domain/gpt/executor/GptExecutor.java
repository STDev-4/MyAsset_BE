package io.api.myasset.domain.gpt.executor;

import io.api.myasset.domain.gpt.dto.GptRequest;
import io.api.myasset.domain.gpt.exception.GptErrorCode;
import io.api.myasset.domain.gpt.prompt.GeneralSystemPrompt;
import io.api.myasset.domain.gpt.prompt.PromptTemplate;
import io.api.myasset.domain.gpt.service.GptService;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GptExecutor {

	private final GptService gptService;
	private final GeneralSystemPrompt generalSystemPrompt = new GeneralSystemPrompt();

	public <T> T execute(
		PromptTemplate domainPrompt,
		PromptTemplate dataPrompt,
		Integer maxTokens,
		Class<T> responseType) {
		PromptTemplate composedPrompt = generalSystemPrompt.compose(domainPrompt, dataPrompt);
		String systemMessage = composedPrompt.render();

		GptRequest request = new GptRequest(
			systemMessage,
			maxTokens != null ? maxTokens : 300);

		String rawContent = gptService.callGpt(request);

		if (rawContent == null) {
			throw new BusinessException(GptErrorCode.GPT_RESPONSE_FORMAT_ERROR);
		}

		return gptService.parseJson(rawContent, responseType);
	}
}
