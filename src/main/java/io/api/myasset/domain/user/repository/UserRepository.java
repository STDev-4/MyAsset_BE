package io.api.myasset.domain.user.repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import io.api.myasset.domain.user.entity.UserTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

	List<User> findByTierOrderByPointDesc(UserTier tier);

	/**
	 * CodefSyncJob 배치 전용 조회.
	 * connectedId 가 존재하는 유저만 선별하며, linkedInstitutions 를 JOIN FETCH 로 함께 로드해
	 * Batch Processor 단계에서 LazyInitializationException 이 발생하지 않도록 한다.
	 * <p>
	 * 페이징 대신 전체 로드를 사용하는 이유: @ElementCollection + JOIN FETCH 조합은 페이징 시
	 * Hibernate 가 메모리 페이징으로 전환하며 HHH90003004 경고를 발생시킨다. 유저 수가 임계치를
	 * 넘어가면 Partition Step 으로 전환한다.
	 */
	@Query("SELECT DISTINCT u FROM User u "
		+ "LEFT JOIN FETCH u.linkedInstitutions "
		+ "WHERE u.connectedId IS NOT NULL")
	List<User> findAllWithConnectedIdAndInstitutions();

	@Query("SELECT DISTINCT u FROM User u "
		+ "LEFT JOIN FETCH u.linkedInstitutions "
		+ "WHERE u.id = :userId AND u.connectedId IS NOT NULL")
	Optional<User> findByIdWithInstitutions(Long userId);
}
