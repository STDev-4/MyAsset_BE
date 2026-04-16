package io.api.myasset.domain.tier.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.character.repository.UserCharacterRepository;
import io.api.myasset.domain.tier.dto.LeagueRankingResponse;
import io.api.myasset.domain.tier.dto.LeagueRankingResponse.RankingEntry;
import io.api.myasset.domain.tier.dto.TierMeResponse;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.tier.exception.TierError;
import io.api.myasset.domain.user.entity.UserTier;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TierService {

	private final UserRepository userRepository;
	private final UserCharacterRepository userCharacterRepository;

	/** GET /api/tier/me - 현재 티어 + 포인트 + 다음 승급까지 */
	@Transactional(readOnly = true)
	public TierMeResponse getMyTier(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));
		return TierMeResponse.from(user);
	}

	/** GET /api/tier/leagues/{tierId} - 해당 티어 전체 랭킹 (포인트 내림차순) */
	@Transactional(readOnly = true)
	public LeagueRankingResponse getLeagueRanking(String tierId) {
		UserTier tier = parseTier(tierId);
		List<User> users = userRepository.findByTierOrderByPointDesc(tier);

		List<Long> userIds = users.stream().map(User::getId).toList();
		Map<Long, String> activeImageMap = userCharacterRepository.findActiveByUserIds(userIds)
			.stream()
			.collect(Collectors.toMap(
				uc -> uc.getUser().getId(),
				uc -> uc.getCharacter().getImageUrl()));

		List<RankingEntry> rankings = new ArrayList<>();
		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			rankings.add(new RankingEntry(
				i + 1,
				user.getId(),
				user.getNickname(),
				user.getPoint(),
				activeImageMap.get(user.getId())));
		}

		return new LeagueRankingResponse(tier.name(), rankings);
	}

	private UserTier parseTier(String tierId) {
		try {
			return UserTier.valueOf(tierId.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new BusinessException(TierError.INVALID_TIER);
		}
	}
}
