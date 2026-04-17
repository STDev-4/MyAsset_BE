package io.api.myasset.global.batch.scheduler;

import io.api.myasset.domain.mission.service.MissionEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "batch.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class MissionEvaluationScheduler {

    private final MissionEvaluationService missionEvaluationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void evaluateMissions() {
        log.info("[MissionEvaluationScheduler] 오전 8시 자동 판정 시작");
        missionEvaluationService.evaluateExpiredMissions();
    }
}