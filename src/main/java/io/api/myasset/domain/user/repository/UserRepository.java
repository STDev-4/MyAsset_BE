package io.api.myasset.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.entity.UserTier;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByLoginId(String loginId);

	boolean existsByEmail(String email);

	Optional<User> findByLoginId(String loginId);

	List<User> findByTierOrderByPointDesc(UserTier tier);
}
