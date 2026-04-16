package io.api.myasset.domain.character.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.api.myasset.domain.character.dto.ActiveCharacterRequest;
import io.api.myasset.domain.character.dto.CharacterListResponse;
import io.api.myasset.domain.character.service.CharacterService;
import io.api.myasset.global.auth.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {

	private final CharacterService characterService;

	/** 캐릭터 목록 조회 - 보유/미보유, 보유 코인, 활성 캐릭터 상태 */
	@GetMapping
	public ResponseEntity<CharacterListResponse> getCharacters() {
		Long userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.ok(characterService.getCharacterList(userId));
	}

	/** 캐릭터 해금 - 코인 차감 후 보유 목록 추가 */
	@PostMapping("/{id}/unlock")
	public ResponseEntity<Void> unlockCharacter(@PathVariable
	Long id) {
		Long userId = SecurityUtil.getCurrentUserId();
		characterService.unlockCharacter(userId, id);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/** 활성 캐릭터 변경 - 보유 중인 캐릭터로만 변경 가능 */
	@PatchMapping("/active")
	public ResponseEntity<Void> changeActiveCharacter(@Valid @RequestBody
	ActiveCharacterRequest request) {
		Long userId = SecurityUtil.getCurrentUserId();
		characterService.changeActiveCharacter(userId, request);
		return ResponseEntity.ok().build();
	}
}
