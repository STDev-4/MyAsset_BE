package io.api.myasset.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.character.entity.UserCharacter;
import io.api.myasset.domain.character.repository.UserCharacterRepository;
import io.api.myasset.domain.user.dto.UserMeResponse;
import io.api.myasset.domain.user.dto.UserMeResponse.ActiveCharacterInfo;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserCharacterRepository userCharacterRepository;

	/** GET /api/users/me - 닉네임, 티어, 포인트, 코인, 활성 캐릭터 */
	@Transactional(readOnly = true)
	public UserMeResponse getMyInfo(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		ActiveCharacterInfo activeCharacter = userCharacterRepository
			.findByUserIdAndActiveTrue(userId)
			.map(UserCharacter::getCharacter)
			.map(character -> new ActiveCharacterInfo(
				character.getId(),
				character.getName(),
				character.getImageUrl()
			))
			.orElse(null);

		return UserMeResponse.of(user, activeCharacter);
	}
}
