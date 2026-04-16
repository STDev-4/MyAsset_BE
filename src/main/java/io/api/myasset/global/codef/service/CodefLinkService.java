package io.api.myasset.global.codef.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.api.myasset.domain.user.entity.InstitutionType;
import io.api.myasset.domain.user.entity.User;
import io.api.myasset.domain.user.exception.UserError;
import io.api.myasset.domain.user.repository.UserRepository;
import io.api.myasset.global.auth.dto.InstitutionCredential;
import io.api.myasset.global.batch.event.BankLinkedEvent;
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
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 유저당 ConnectedId는 1개.
	 * - CID 없음: /v1/account/create → CID 발급 후 User에 저장
	 * - CID 있음: /v1/account/add   → 기존 CID에 기관 추가
	 */
	@Transactional
	public CodefLinkResponse link(Long userId, CodefLinkRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

		List<InstitutionType> linked = new ArrayList<>();
		List<InstitutionType> failed = new ArrayList<>();

		for (InstitutionCredential credential : request.institutions()) {
			boolean success = linkInstitution(user, credential);

			if (success) {
				user.addLinkedInstitution(credential.institutionType());
				linked.add(credential.institutionType());
			} else {
				log.warn("[CodefLink] 기관 연동 실패 - userId={}, institution={}",
					userId, credential.institutionType().getDisplayName());
				failed.add(credential.institutionType());
			}
		}

		if (hasCardInstitution(linked)) {
			eventPublisher.publishEvent(new BankLinkedEvent(userId));
		}

		return new CodefLinkResponse(linked, failed);
	}

	private boolean linkInstitution(User user, InstitutionCredential credential) {
		if (!user.hasConnectedId()) {
			Optional<String> connectedId = codefConnectedIdService.createConnectedId(
				credential.institutionType(),
				credential.loginId(),
				credential.loginPassword());

			connectedId.ifPresent(user::assignConnectedId);
			return connectedId.isPresent();
		}

		return codefConnectedIdService.addAccount(
			user.getConnectedId(),
			credential.institutionType(),
			credential.loginId(),
			credential.loginPassword());
	}

	/**
	 * 카드 기관이 연동된 경우에만 Sync 가 의미 있으므로 이벤트 발행 여부를 제어한다.
	 */
	private boolean hasCardInstitution(List<InstitutionType> linked) {
		return linked.stream()
			.anyMatch(t -> t.getCategory() == InstitutionType.Category.CARD);
	}
}
