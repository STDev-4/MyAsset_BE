package io.api.myasset.domain.gpt.client;

import io.api.myasset.domain.gpt.dto.GptRequest;
import io.api.myasset.domain.gpt.dto.GptResponse;
import io.api.myasset.domain.gpt.exception.GptErrorCode;
import io.api.myasset.global.exception.error.BusinessException;
import io.api.myasset.global.webclient.ApiClient;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class GptClient extends ApiClient {

	public GptClient(@Qualifier("gptWebClient")
	WebClient webClient) {
		super(webClient);
	}

	public GptResponse requestCompletion(GptRequest request) {
		try {
			log.info("[GptClient] GPT 요청 시작 - model={}", request.getModel());
			log.debug("[GptClient] request={}", request);

			GptResponse response = post(
				uri -> uri.path("/chat/completions").build(),
				request,
				GptResponse.class);

			log.info("[GptClient] GPT 응답 성공");
			log.debug("[GptClient] response={}", response);

			return response;

		} catch (WebClientResponseException.TooManyRequests e) {
			log.error("[GptClient] 429 Too Many Requests - body={}", e.getResponseBodyAsString(), e);
			throw new BusinessException(GptErrorCode.GPT_TOO_MANY_REQUESTS);

		} catch (WebClientResponseException e) {
			log.error(
				"[GptClient] GPT API 오류 - status={}, body={}",
				e.getStatusCode(),
				e.getResponseBodyAsString(),
				e);
			throw new BusinessException(GptErrorCode.GPT_API_ERROR);

		} catch (ReadTimeoutException e) {
			log.error("[GptClient] GPT Timeout", e);
			throw new BusinessException(GptErrorCode.GPT_TIMEOUT);

		} catch (Exception e) {
			log.error("[GptClient] GPT Unknown Error - request={}", request, e);
			throw new BusinessException(GptErrorCode.GPT_UNKNOWN_ERROR);
		}
	}
}
