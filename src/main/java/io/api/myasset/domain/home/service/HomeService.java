package io.api.myasset.domain.home.service;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.home.dto.response.HomeRankingPercentileResponse;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final UserRepository userRepository;

    // 절약력 상위 퍼센트 조회
    public HomeRankingPercentileResponse getRankingPercentile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        Integer myPoint = user.getPoint();
        long totalUserCount = userRepository.count();

        if (totalUserCount == 0) {
            return HomeRankingPercentileResponse.builder()
                    .rankingPercentile("0.0")
                    .measuredAt(LocalDate.now())
                    .build();
        }

        long higherUserCount = userRepository.countByPointGreaterThan(myPoint);
        long rank = higherUserCount + 1;

        double calculatedPercentile = (((double) rank - 0.5) * 100) / totalUserCount;
        calculatedPercentile = Math.round(calculatedPercentile * 10) / 10.0;

        String rankingPercentile = String.format(Locale.US, "%.1f", calculatedPercentile);

        return HomeRankingPercentileResponse.builder()
                .rankingPercentile(rankingPercentile)
                .measuredAt(LocalDate.now())
                .build();
    }
}