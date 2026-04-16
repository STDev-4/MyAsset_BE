package io.api.myasset.domain.character.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.api.myasset.domain.character.entity.UserCharacter;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {

	List<UserCharacter> findByUserId(Long userId);

	Optional<UserCharacter> findByUserIdAndActiveTrue(Long userId);

	Optional<UserCharacter> findByUserIdAndCharacterId(Long userId, Long characterId);

	boolean existsByUserIdAndCharacterId(Long userId, Long characterId);

	/** 리그 랭킹 조회용 - N+1 방지 배치 쿼리 */
	@Query("SELECT uc FROM UserCharacter uc JOIN FETCH uc.character WHERE uc.user.id IN :userIds AND uc.active = true")
	List<UserCharacter> findActiveByUserIds(@Param("userIds")
	List<Long> userIds);
}
