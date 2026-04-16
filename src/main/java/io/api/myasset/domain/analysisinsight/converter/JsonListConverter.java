package io.api.myasset.domain.analysisinsight.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.api.myasset.domain.analysisinsight.exception.AnalysisError;

import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonListConverter {

    private final ObjectMapper objectMapper;

    public List<String> toList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException(AnalysisError.ACTION_TIPS_JSON_PARSE_ERROR);
        }
    }

    public String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new BusinessException(AnalysisError.ACTION_TIPS_JSON_SERIALIZE_ERROR);
        }
    }
}