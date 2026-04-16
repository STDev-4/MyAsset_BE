package io.api.myasset.domain.league.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.api.myasset.domain.league.dto.response.LeagueSelectedRankingResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.league.dto.response.LeagueRankingResponse;
import io.api.myasset.domain.league.dto.response.LeagueRankingUserResponse;
import io.api.myasset.domain.league.dto.response.MyLeagueInfoResponse;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.entity.UserTier;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.exception.error.BusinessException;
import io.api.myasset.global.exception.error.GlobalError;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueService {

    private final UserRepository userRepository;

    // 리그 랭킹 조회
    public LeagueRankingResponse getLeagueRanking(Long userId, int size) {

        User me = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(GlobalError.INVALID_VALUE));

        UserTier tier = me.getTier();
        PageRequest pageRequest = PageRequest.of(0, size);

        List<User> users = userRepository.findAllByTierOrderByPointDesc(tier, pageRequest);
        long totalUserCount = userRepository.countByTier(tier);

        List<LeagueRankingUserResponse> rankingList = new ArrayList<>();
        int myRank = 0;
        int rank = 1;

        for (User user : users) {
            boolean active = isActive(user.getLastLoginAt());

            LeagueRankingUserResponse rankingUserResponse = LeagueRankingUserResponse.builder()
                    .rank(rank)
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .point(user.getPoint())
                    .lastLoginAt(formatLastLogin(user.getLastLoginAt()))
                    .isActive(active)
                    .build();

            rankingList.add(rankingUserResponse);

            if (user.getId().equals(me.getId())) {
                myRank = rank;
            }

            rank++;
        }

        MyLeagueInfoResponse myInfo = MyLeagueInfoResponse.builder()
                .nickname(me.getNickname())
                .profileImageUrl(me.getProfileImageUrl())
                .point(me.getPoint())
                .tier(me.getTier().getLabel())
                .rank(myRank)
                .lastLoginAt(formatLastLogin(me.getLastLoginAt()))
                .isActive(isActive(me.getLastLoginAt()))
                .build();

        LocalDateTime endTime = getNextResetTime();
        String remainingTime = calculateRemainingTime(endTime);

        return LeagueRankingResponse.builder()
                .myInfo(myInfo)
                .remainingTime(remainingTime)
                .totalUserCount((int) totalUserCount)
                .rankings(rankingList)
                .build();
    }

    // 다음 랭킹 리셋 시간 계산
    private LocalDateTime getNextResetTime() {
        return LocalDateTime.now()
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    // 활동중 여부 계산
    private boolean isActive(LocalDateTime lastLoginAt) {
        if (lastLoginAt == null) {
            return false;
        }

        return lastLoginAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    // 마지막 접속 시간 문자열 변환
    private String formatLastLogin(LocalDateTime lastLoginAt) {
        if (lastLoginAt == null) {
            return "정보 없음";
        }

        Duration duration = Duration.between(lastLoginAt, LocalDateTime.now());

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 60) {
            return minutes + "분 전";
        }

        if (hours < 24) {
            return hours + "시간 전";
        }

        return days + "일 전";
    }

    // 랭킹 재배치 남은 시간 계산
    private String calculateRemainingTime(LocalDateTime endTime) {
        Duration duration = Duration.between(LocalDateTime.now(), endTime);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        return days + "일 " + hours + "시간 " + minutes + "분";
    }

    // 선택한 리그 랭킹 조회
    public LeagueSelectedRankingResponse getLeagueSelectedRanking(UserTier selectedTier, int size) {
        PageRequest pageRequest = PageRequest.of(0, size);

        List<User> users = userRepository.findAllByTierOrderByPointDesc(selectedTier, pageRequest);
        long totalUserCount = userRepository.countByTier(selectedTier);

        List<LeagueRankingUserResponse> rankingList = new ArrayList<>();
        int rank = 1;

        for (User user : users) {
            boolean active = isActive(user.getLastLoginAt());

            LeagueRankingUserResponse rankingUserResponse = LeagueRankingUserResponse.builder()
                    .rank(rank)
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .point(user.getPoint())
                    .lastLoginAt(formatLastLogin(user.getLastLoginAt()))
                    .isActive(active)
                    .build();

            rankingList.add(rankingUserResponse);
            rank++;
        }

        String remainingTime = calculateRemainingTime(getNextResetTime());

        return LeagueSelectedRankingResponse.builder()
                .remainingTime(remainingTime)
                .totalUserCount((int) totalUserCount)
                .rankings(rankingList)
                .build();
    }
}