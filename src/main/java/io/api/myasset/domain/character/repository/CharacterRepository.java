package io.api.myasset.domain.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.api.myasset.domain.character.entity.Character;

public interface CharacterRepository extends JpaRepository<Character, Long> {}
