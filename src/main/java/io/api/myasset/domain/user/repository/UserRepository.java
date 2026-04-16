package io.api.myasset.domain.user.repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import io.api.myasset.domain.user.entity.UserTier;
import org.springframework.data.jpa.repository.JpaRepository;

import io.api.myasset.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByLoginId(String loginId);

	boolean existsByEmail(String email);

	Optional<User> findByLoginId(String loginId);

	// 특정 티어 사용자 수 조회
	long countByTier(UserTier tier);

	// 티어 기준 랭킹 조회 (페이징)
	List<User> findAllByTierOrderByPointDesc(UserTier tier, Pageable pageable);

	// 현재 유저보다 point가 높은 유저 수 조회
	long countByPointGreaterThan(Integer point);
}
