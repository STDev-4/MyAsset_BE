package io.api.myasset.domain.analysisinsight.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.api.myasset.domain.analysisinsight.dto.AnalysisInsightItemResponse;
import io.api.myasset.domain.analysisinsight.exception.AnalysisError;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisInsightCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public List<AnalysisInsightItemResponse> getInsights(Long userId, LocalDate date) {
        String key = createInsightKey(userId, date);
        String json = redisTemplate.opsForValue().get(key);

        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, new TypeReference<List<AnalysisInsightItemResponse>>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException(AnalysisError.INSIGHT_REDIS_DESERIALIZE_ERROR);
        }
    }

    public void saveInsights(Long userId, LocalDate date, List<AnalysisInsightItemResponse> insights) {
        String key = createInsightKey(userId, date);

        try {
            String json = objectMapper.writeValueAsString(insights);
            redisTemplate.opsForValue().set(key, json, getDurationUntilEndOfDay());
        } catch (JsonProcessingException e) {
            throw new BusinessException(AnalysisError.INSIGHT_REDIS_SERIALIZE_ERROR);
        }
    }

    public void evictInsights(Long userId, LocalDate date) {
        redisTemplate.delete(createInsightKey(userId, date));
    }

    private String createInsightKey(Long userId, LocalDate date) {
        return "analysis:insights:" + userId + ":" + date.toString().replace("-", "");
    }

    private Duration getDurationUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        return Duration.between(now, endOfDay);
    }
}