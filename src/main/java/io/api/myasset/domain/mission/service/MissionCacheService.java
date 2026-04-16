package io.api.myasset.domain.mission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.api.myasset.domain.mission.dto.RecommendedMissionResponse;
import io.api.myasset.domain.mission.exception.MissionError;
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
public class MissionCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public List<RecommendedMissionResponse> getRecommendedMissions(Long userId, LocalDate date) {
        String key = createRecommendedMissionKey(userId, date);
        String json = redisTemplate.opsForValue().get(key);

        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, new TypeReference<List<RecommendedMissionResponse>>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException(MissionError.MISSION_REDIS_DESERIALIZE_ERROR);
        }
    }

    public void saveRecommendedMissions(Long userId, LocalDate date, List<RecommendedMissionResponse> missions) {
        String key = createRecommendedMissionKey(userId, date);

        try {
            String json = objectMapper.writeValueAsString(missions);
            redisTemplate.opsForValue().set(key, json, getDurationUntilEndOfDay());
        } catch (JsonProcessingException e) {
            throw new BusinessException(MissionError.MISSION_REDIS_SERIALIZE_ERROR);
        }
    }

    public void evictRecommendedMissions(Long userId, LocalDate date) {
        redisTemplate.delete(createRecommendedMissionKey(userId, date));
    }

    private String createRecommendedMissionKey(Long userId, LocalDate date) {
        return "mission:recommended:" + userId + ":" + date.toString().replace("-", "");
    }

    private Duration getDurationUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        return Duration.between(now, endOfDay);
    }
}