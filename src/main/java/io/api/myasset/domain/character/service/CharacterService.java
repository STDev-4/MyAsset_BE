package io.api.myasset.domain.character.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.character.dto.ActiveCharacterRequest;
import io.api.myasset.domain.character.dto.CharacterListResponse;
import io.api.myasset.domain.character.dto.CharacterResponse;
import io.api.myasset.domain.character.entity.Character;
import io.api.myasset.domain.character.entity.UserCharacter;
import io.api.myasset.domain.character.exception.CharacterError;
import io.api.myasset.domain.character.repository.CharacterRepository;
import io.api.myasset.domain.character.repository.UserCharacterRepository;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CharacterService {

	private final CharacterRepository characterRepository;
	private final UserCharacterRepository userCharacterRepository;
	private final UserRepository userRepository;

	/** GET /api/characters - 전체 캐릭터 목록 + 보유 여부 + 활성 상태 + 보유 코인 */
	@Transactional(readOnly = true)
	public CharacterListResponse getCharacterList(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));
		List<Character> allCharacters = characterRepository.findAll();
		List<UserCharacter> userCharacters = userCharacterRepository.findByUserId(userId);

		// O(n) Map 조회 — 기존 O(n²) stream filter 루프 개선
		Map<Long, UserCharacter> userCharacterMap = userCharacters.stream()
			.collect(Collectors.toMap(uc -> uc.getCharacter().getId(), uc -> uc));

		List<CharacterResponse> characters = allCharacters.stream()
			.map(character -> {
				UserCharacter uc = userCharacterMap.get(character.getId());
				return uc != null ? CharacterResponse.ofOwned(uc) : CharacterResponse.ofUnowned(character);
			})
			.toList();

		return new CharacterListResponse(characters, user.getCoin());
	}

	/** POST /api/characters/{id}/unlock - 코인 차감 후 캐릭터 해금 */
	@Transactional
	public void unlockCharacter(Long userId, Long characterId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));
		Character character = characterRepository.findById(characterId)
			.orElseThrow(() -> new BusinessException(CharacterError.CHARACTER_NOT_FOUND));

		if (userCharacterRepository.existsByUserIdAndCharacterId(userId, characterId)) {
			throw new BusinessException(CharacterError.ALREADY_UNLOCKED);
		}
		if (!user.hasSufficientCoin(character.getCoinPrice())) {
			throw new BusinessException(CharacterError.INSUFFICIENT_COIN);
		}

		user.deductCoin(character.getCoinPrice());
		userCharacterRepository.save(UserCharacter.unlock(user, character));
	}

	/**
	 * PATCH /api/characters/active - 보유 캐릭터로 활성 변경.
	 * 기존 활성 캐릭터를 먼저 비활성화한 뒤 target을 활성화해 유저당 active=true 1개를 보장.
	 * DB 레벨 partial unique index(WHERE active=true)는 JPA 미지원으로 Flyway 도입 시 추가 필요.
	 */
	@Transactional
	public void changeActiveCharacter(Long userId, ActiveCharacterRequest request) {
		UserCharacter target = userCharacterRepository
			.findByUserIdAndCharacterId(userId, request.characterId())
			.orElseThrow(() -> new BusinessException(CharacterError.CHARACTER_NOT_OWNED));

		// 기존 활성 캐릭터 비활성화 — target과 동일하면 중복 처리 방지
		userCharacterRepository.findByUserIdAndActiveTrue(userId)
			.filter(current -> !current.getId().equals(target.getId()))
			.ifPresent(UserCharacter::deactivate);

		target.activate();
	}
}
