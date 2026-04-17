package io.api.myasset.domain.character.entity;

import java.time.LocalDateTime;

import io.api.myasset.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_characters", uniqueConstraints = @UniqueConstraint(name = "uk_user_character", columnNames = {
	"user_id", "character_id"}))
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCharacter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "character_id", nullable = false)
	private Character character;

	@Column(nullable = false)
	@Builder.Default
	private boolean active = false;

	@Column(name = "unlocked_at", nullable = false)
	private LocalDateTime unlockedAt;

	public static UserCharacter unlock(User user, Character character) {
		return UserCharacter.builder()
			.user(user)
			.character(character)
			.unlockedAt(LocalDateTime.now())
			.build();
	}

	/** 회원가입 시 기본 캐릭터 배정 — 즉시 활성 상태로 생성 */
	public static UserCharacter assignDefault(User user, Character character) {
		return UserCharacter.builder()
			.user(user)
			.character(character)
			.active(true)
			.unlockedAt(LocalDateTime.now())
			.build();
	}

	public void activate() {
		this.active = true;
	}

	public void deactivate() {
		this.active = false;
	}
}
