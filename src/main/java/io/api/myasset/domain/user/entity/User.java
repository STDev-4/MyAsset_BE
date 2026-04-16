package io.api.myasset.domain.user.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import jakarta.persistence.Table;
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

	//사용자 캐릭터 url
	@Column(name = "profile_image_url")
	private String profileImageUrl;

	// 포인트
	@Column(nullable = false)
	@Builder.Default
	private Integer point = 0;

	//마지막 접속 시간
	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	//유저의 티어
	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserTier tier = UserTier.SEED;

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

	// 마지막 로그인 시간 업데이트
	public void updateLastLoginAt() {
		this.lastLoginAt = LocalDateTime.now();
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
}
