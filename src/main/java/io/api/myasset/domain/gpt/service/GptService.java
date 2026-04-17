package io.api.myasset.domain.gpt.service;

import io.api.myasset.domain.gpt.exception.GptErrorCode;
import io.api.myasset.domain.gpt.client.GptClient;
import io.api.myasset.domain.gpt.dto.ExampleGptResponse;
import io.api.myasset.domain.gpt.dto.GptRequest;
import io.api.myasset.domain.gpt.prompt.GeneralSystemPrompt;
import io.api.myasset.domain.gpt.prompt.PromptTemplate;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GptService {

    private final GptClient gptClient;
    private final GeneralSystemPrompt generalSystemPrompt = new GeneralSystemPrompt();

    public String callGpt(GptRequest gptRequest){

        // token 수 설정
        Integer maxTokens =
                gptRequest.getMaxTokens() != null
                        ? gptRequest.getMaxTokens()
                        : 300;

        gptRequest.setMaxTokens(maxTokens);

        // OpenAi 호출
        return gptClient.requestCompletion(gptRequest).firstContent();
    }

    /**
     * 예시용 호출 메서드
     * @param domainPrompt 도메인별 프롬프트
     * @param dataPrompt 학습할 데이터
     * @param maxTokens 최대 토큰 수 커스텀
     * @return
     */
    public ExampleGptResponse getAssistantMsg(
            PromptTemplate domainPrompt,
            PromptTemplate dataPrompt,
            Integer maxTokens
    ) {
        // 프롬프트 조합
        PromptTemplate composedPrompt =
                generalSystemPrompt.compose(domainPrompt, dataPrompt);
        String systemMessage = composedPrompt.render();

        // GptRequest 생성
        GptRequest request = new GptRequest(
                systemMessage,
                maxTokens != null ? maxTokens : 300
        );

        // GPT 호출
        String rawContent = callGpt(request);

        if (rawContent == null) {
            throw new BusinessException(GptErrorCode.GPT_RESPONSE_FORMAT_ERROR);
        }

        // 파싱/검증
        return parseJson(rawContent, ExampleGptResponse.class);
    }

    // json으로 파싱
    public <T> T parseJson(String rawContent, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper()
                    .configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false
                    );

            return mapper.readValue(rawContent, clazz);

        } catch (Exception e) {
            throw new BusinessException(GptErrorCode.GPT_PARSE_ERROR);
        }
    }

}
