package io.api.myasset.domain.mission.service;

import io.api.myasset.domain.mission.dto.CachedRecommendedMission;
import io.api.myasset.domain.mission.provider.MissionJsonProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MissionCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MissionJsonProvider missionJsonProvider;

    private String recommendedMissionKey(Long userId, LocalDate date) {
        return "mission:recommended:" + userId + ":" + date.toString().replace("-", "");
    }

    public List<CachedRecommendedMission> getRecommendedMissionCache(Long userId, LocalDate date) {
        String value = redisTemplate.opsForValue().get(recommendedMissionKey(userId, date));
        if (value == null || value.isBlank()) {
            return null;
        }
        return missionJsonProvider.toRecommendedMissionList(value);
    }

    public void saveRecommendedMissionCache(Long userId, LocalDate date, List<CachedRecommendedMission> missions) {
        redisTemplate.opsForValue().set(
                recommendedMissionKey(userId, date),
                missionJsonProvider.toRecommendedMissionJson(missions),
                1,
                TimeUnit.DAYS
        );
    }

    public void evictRecommendedMissionCache(Long userId, LocalDate date) {
        redisTemplate.delete(recommendedMissionKey(userId, date));
    }
}