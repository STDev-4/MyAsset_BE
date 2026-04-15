package io.api.myasset.global.codef.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.user.domain.CodefAccount;
import io.api.myasset.domain.user.domain.InstitutionType;
import io.api.myasset.domain.user.domain.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.auth.dto.InstitutionCredential;
import io.api.myasset.global.codef.CodefConnectedIdService;
import io.api.myasset.global.codef.dto.CodefLinkRequest;
import io.api.myasset.global.codef.dto.CodefLinkResponse;
import io.api.myasset.global.exception.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefLinkService {

	private final UserRepository userRepository;
	private final CodefConnectedIdService codefConnectedIdService;

	/**
	 * 인증된 사용자의 금융기관 계정을 Codef에 연동한다.
	 * 연동에 성공한 기관은 CodefAccount로 저장하고, 실패한 기관은 failed 목록으로 반환한다.
	 */
	@Transactional
	public CodefLinkResponse link(Long userId, CodefLinkRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		List<InstitutionType> linked = new ArrayList<>();
		List<InstitutionType> failed = new ArrayList<>();

		for (InstitutionCredential credential : request.institutions()) {
			Optional<String> connectedId = codefConnectedIdService.createConnectedId(
				credential.institutionType(),
				credential.loginId(),
				credential.loginPassword()
			);

			if (connectedId.isPresent()) {
				user.addCodefAccount(CodefAccount.create(connectedId.get(), credential.institutionType()));
				linked.add(credential.institutionType());
			} else {
				log.warn("[CodefLink] 기관 연동 실패 - userId={}, institution={}",
					userId, credential.institutionType().getDisplayName());
				failed.add(credential.institutionType());
			}
		}

		return new CodefLinkResponse(linked, failed);
	}
}
