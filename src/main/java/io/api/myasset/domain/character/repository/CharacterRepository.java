package io.api.myasset.domain.character.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.api.myasset.domain.character.entity.Character;

public interface CharacterRepository extends JpaRepository<Character, Long> {

	/** coin_price = 0 인 캐릭터 중 첫 번째 — 회원가입 기본 캐릭터 배정용 */
	Optional<Character> findFirstByCoinPrice(int coinPrice);
}
