package io.api.myasset.domain.gpt.client;

import io.api.myasset.domain.gpt.dto.GptRequest;
import io.api.myasset.domain.gpt.dto.GptResponse;
import io.api.myasset.domain.gpt.exception.GptErrorCode;
import io.api.myasset.global.exception.error.BusinessException;
import io.api.myasset.global.webclient.ApiClient;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class GptClient extends ApiClient {

    public GptClient(@Qualifier("gptWebClient")WebClient webClient){
        super(webClient);
    }

    public GptResponse requestCompletion(GptRequest request) {
        try {
            return post(
                    uri -> uri.path("/chat/completions").build(),
                    request,
                    GptResponse.class
            );
        } catch (WebClientResponseException.TooManyRequests e) {
            // 429 에러 : GPT Rate Limit
            throw new BusinessException(GptErrorCode.GPT_TOO_MANY_REQUESTS);

        } catch (WebClientResponseException e) {
            // GPT 서버 응답 오류
            throw new BusinessException(GptErrorCode.GPT_API_ERROR);

        } catch (ReadTimeoutException e) {
            // 네트워크/응답 지연
            throw new BusinessException(GptErrorCode.GPT_TIMEOUT);

        } catch (Exception e) {
            // 그 외 오류 처리
            throw new BusinessException(GptErrorCode.GPT_UNKNOWN_ERROR);
        }
    }

}
