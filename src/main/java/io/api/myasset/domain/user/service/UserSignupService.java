package io.api.myasset.domain.user.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.user.domain.CodefAccount;
import io.api.myasset.domain.user.domain.User;
import io.api.myasset.domain.user.dto.request.InstitutionCredential;
import io.api.myasset.domain.user.dto.request.SignupRequest;
import io.api.myasset.domain.user.dto.response.SignupResponse;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.codef.CodefConnectedIdService;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserSignupService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final CodefConnectedIdService codefConnectedIdService;

	public SignupResponse signup(SignupRequest request) {
		validateDuplication(request.loginId(), request.email());

		User user = User.create(
			request.loginId(),
			passwordEncoder.encode(request.password()),
			request.email(),
			request.nickname(),
			request.birthDate()
		);

		for (InstitutionCredential credential : request.institutions()) {
			Optional<String> connectedId = codefConnectedIdService.createConnectedId(
				credential.institutionType(),
				credential.loginId(),
				credential.loginPassword()
			);

			connectedId.ifPresentOrElse(
				id -> user.addCodefAccount(CodefAccount.create(id, credential.institutionType())),
				() -> log.warn("[Signup] 기관 연동 건너뜀 - institution={}",
					credential.institutionType().getDisplayName())
			);
		}

		User savedUser = userRepository.save(user);
		return SignupResponse.from(savedUser);
	}

	private void validateDuplication(String loginId, String email) {
		if (userRepository.existsByLoginId(loginId)) {
			throw new BusinessException(UserError.DUPLICATE_LOGIN_ID);
		}
		if (userRepository.existsByEmail(email)) {
			throw new BusinessException(UserError.DUPLICATE_EMAIL);
		}
	}
}
