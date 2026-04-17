package io.api.myasset.domain.mission.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.api.myasset.domain.mission.dto.CachedRecommendedMission;
import io.api.myasset.domain.mission.exception.MissionError;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MissionJsonProvider {

    private final ObjectMapper objectMapper;

    public String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new BusinessException(MissionError.MISSION_REDIS_SERIALIZE_ERROR);
        }
    }

    public List<String> toList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException(MissionError.MISSION_REDIS_DESERIALIZE_ERROR);
        }
    }

    public String toRecommendedMissionJson(List<CachedRecommendedMission> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new BusinessException(MissionError.MISSION_REDIS_SERIALIZE_ERROR);
        }
    }

    public List<CachedRecommendedMission> toRecommendedMissionList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<CachedRecommendedMission>>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException(MissionError.MISSION_REDIS_DESERIALIZE_ERROR);
        }
    }
}