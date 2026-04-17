package io.api.myasset.domain.user.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.api.myasset.domain.user.exception.UserError;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.api.myasset.domain.character.entity.UserCharacter;
import io.api.myasset.domain.character.exception.CharacterError;
import io.api.myasset.global.exception.error.BusinessException;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 낙관적 락 버전 필드.
	 * 코인 차감 등 동시 요청이 가능한 UPDATE 시 충돌 감지용.
	 * 두 트랜잭션이 동시에 같은 row를 수정하면 나중에 커밋하는 쪽이 OptimisticLockException 발생.
	 */
	@Version
	private Long version;

	@Column(nullable = false, unique = true)
	private String loginId;

	@JsonIgnore
	@Column(nullable = false, length = 60)
	private String password;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String nickname;

	@Column(nullable = false)
	private LocalDate birthDate;

	@Column
	private String connectedId;

	@Column(nullable = false, columnDefinition = "INT DEFAULT 0")
	@Builder.Default
	private Integer point = 0;

	@Column(nullable = false, columnDefinition = "INT DEFAULT 0")
	@Builder.Default
	private Integer coin = 0;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'SEED'")
	private UserTier tier = UserTier.SEED;

	@Builder.Default
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserCharacter> userCharacters = new ArrayList<>();

	@Builder.Default
	@ElementCollection
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "user_linked_institutions", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "institution_type", nullable = false)
	private List<InstitutionType> linkedInstitutions = new ArrayList<>();

	public static User create(
		String loginId,
		String encodedPassword,
		String email,
		String nickname,
		LocalDate birthDate) {
		return User.builder()
			.loginId(loginId)
			.password(encodedPassword)
			.email(email)
			.nickname(nickname)
			.birthDate(birthDate)
			.build();
	}

	public boolean matchesPassword(String rawPassword, PasswordEncoder passwordEncoder) {
		return passwordEncoder.matches(rawPassword, this.password);
	}

	public boolean hasConnectedId() {
		return this.connectedId != null;
	}

	public void assignConnectedId(String connectedId) {
		this.connectedId = connectedId;
	}

	public void updateLastLoginAt() {
		this.lastLoginAt = LocalDateTime.now();
	}

	/**
	 * 코인 차감.
	 * 서비스 레이어에서 hasSufficientCoin() 체크 후 호출하더라도,
	 * 엔티티 레벨에서 한 번 더 방어해 음수 코인 저장을 원천 차단.
	 */
	public void deductCoin(int amount) {
		if (this.coin < amount) {
			throw new BusinessException(CharacterError.INSUFFICIENT_COIN);
		}
		this.coin -= amount;
	}

	public boolean hasSufficientCoin(int amount) {
		return this.coin >= amount;
	}

	public void addLinkedInstitution(InstitutionType institutionType) {
		linkedInstitutions.add(institutionType);
	}

	public List<InstitutionType> getSecuritiesInstitutions() {
		return linkedInstitutions.stream()
			.filter(t -> t.getCategory() == InstitutionType.Category.INVEST)
			.toList();
	}

	public List<InstitutionType> getBankInstitutions() {
		return linkedInstitutions.stream()
			.filter(t -> t.getCategory() == InstitutionType.Category.BANK)
			.toList();
	}

	public List<InstitutionType> getCardInstitutions() {
		return linkedInstitutions.stream()
			.filter(t -> t.getCategory() == InstitutionType.Category.CARD)
			.toList();
	}

    public void addPoint(int amount) {
        if (amount < 0) {
            throw new BusinessException(UserError.INVALID_POINT_AMOUNT);
        }
        this.point += amount;
    }
}
