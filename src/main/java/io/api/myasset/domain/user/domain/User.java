package io.api.myasset.domain.user.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

	@Builder.Default
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CodefAccount> codefAccounts = new ArrayList<>();

	public static User create(
		String loginId,
		String encodedPassword,
		String email,
		String nickname,
		LocalDate birthDate
	) {
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

	public void addCodefAccount(CodefAccount codefAccount) {
		codefAccounts.add(codefAccount);
		codefAccount.assignUser(this);
	}

	public List<CodefAccount> getSecuritiesAccounts() {
		return codefAccounts.stream()
			.filter(c -> c.getInstitutionType().getCategory() == InstitutionType.Category.SECURITIES)
			.toList();
	}

	public List<CodefAccount> getBankAccounts() {
		return codefAccounts.stream()
			.filter(c -> c.getInstitutionType().getCategory() == InstitutionType.Category.BANK)
			.toList();
	}

	public List<CodefAccount> getCardAccounts() {
		return codefAccounts.stream()
			.filter(c -> c.getInstitutionType().getCategory() == InstitutionType.Category.CARD)
			.toList();
	}
}
