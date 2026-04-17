package io.api.myasset.domain.analysisinsight.controller;

import io.api.myasset.domain.analysisinsight.dto.AnalysisInsightItemResponse;
import io.api.myasset.domain.analysisinsight.service.AnalysisInsightService;
import io.api.myasset.global.auth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisInsightController {

    private final AnalysisInsightService analysisInsightService;

    @GetMapping("/insights")
    public ResponseEntity<List<AnalysisInsightItemResponse>> getInsights() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(analysisInsightService.getInsights(userId));
    }
}