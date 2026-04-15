package io.api.myasset.global.codef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.api.myasset.domain.user.domain.InstitutionType;
import io.codef.api.EasyCodefClient;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefRequestBuilder;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.util.RsaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodefConnectedIdService {

	// 계정 관리 API는 /kr prefix 없음 (상품 조회 API와 다름)
	private static final String ACCOUNT_CREATE_URL = "/v1/account/create";
	private static final String SUCCESS_CODE = "CF-00000";
	private static final String COUNTRY_CODE = "KR";
	private static final String LOGIN_TYPE_ID_PW = "1";

	private final EasyCodefClient easyCodefClient;

	/**
	 * Codef에 기관 계정을 등록하고 커넥티드아이디를 발급받는다.
	 * 연동 실패 시 empty를 반환하며 회원가입 자체는 중단하지 않는다.
	 *
	 * @param institutionType 연동할 기관 유형
	 * @param loginId         기관 로그인 아이디
	 * @param loginPassword   기관 로그인 비밀번호 (평문)
	 * @return 커넥티드아이디 (실패 시 Optional.empty)
	 */
	public Optional<String> createConnectedId(
		InstitutionType institutionType,
		String loginId,
		String loginPassword
	) {
		try {
			String encryptedPassword = RsaUtil.encryptRsa(loginPassword, easyCodefClient.getPublicKey());

			Map<String, Object> params = buildParams(institutionType, loginId, encryptedPassword);

			EasyCodefRequest request = EasyCodefRequestBuilder.builder()
				.productUrl(ACCOUNT_CREATE_URL)
				.parameterMap(params)
				.build();

			EasyCodefResponse response = easyCodefClient.requestProduct(request);
			log.info("[Codef] 계정 등록 응답: {}", response);

			if (!SUCCESS_CODE.equals(response.getResult().getCode())) {
				log.warn("[Codef] 커넥티드아이디 발급 실패 - institution={}, code={}, message={}",
					institutionType.getDisplayName(),
					response.getResult().getCode(),
					response.getResult().getMessage());
				return Optional.empty();
			}

			return extractConnectedId(response.getData());

		} catch (Exception e) {
			log.warn("[Codef] 커넥티드아이디 발급 중 예외 발생 - institution={}, reason={}",
				institutionType.getDisplayName(), e.getMessage());
			return Optional.empty();
		}
	}

	private Map<String, Object> buildParams(
		InstitutionType institutionType,
		String loginId,
		String encryptedPassword
	) {
		Map<String, Object> account = new HashMap<>();
		account.put("countryCode", COUNTRY_CODE);
		account.put("businessType", institutionType.getCodefBusinessType());
		account.put("clientType", institutionType.getCodefClientType());
		account.put("organization", institutionType.getCodefOrgCode());
		account.put("loginType", LOGIN_TYPE_ID_PW);
		account.put("id", loginId);
		account.put("password", encryptedPassword);

		List<Map<String, Object>> accountList = new ArrayList<>();
		accountList.add(account);

		Map<String, Object> params = new HashMap<>();
		params.put("accountList", accountList);
		return params;
	}

	/**
	 * 응답 data 구조: {"successList": [{"connectedId": "...", ...}], "failList": [...]}
	 */
	@SuppressWarnings("unchecked")
	private Optional<String> extractConnectedId(Object data) {
		if (!(data instanceof Map<?, ?> dataMap)) {
			return Optional.empty();
		}

		Object successListRaw = dataMap.get("successList");
		if (!(successListRaw instanceof List<?> successList) || successList.isEmpty()) {
			return Optional.empty();
		}

		Object first = successList.get(0);
		if (!(first instanceof Map<?, ?> firstMap)) {
			return Optional.empty();
		}

		Object connectedId = firstMap.get("connectedId");
		if (connectedId instanceof String id && !id.isBlank()) {
			return Optional.of(id);
		}

		return Optional.empty();
	}
}
