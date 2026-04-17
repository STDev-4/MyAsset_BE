package io.api.myasset.global.codef;

import static io.api.myasset.global.codef.constant.CodefUrl.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.api.myasset.domain.user.entity.InstitutionType;
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

	private static final String SUCCESS_CODE = "CF-00000";
	private static final String COUNTRY_CODE = "KR";
	private static final String LOGIN_TYPE_ID_PW = "1";

	private final EasyCodefClient easyCodefClient;

	/**
	 * 최초 연동 시 호출. 새 ConnectedId를 발급받는다.
	 * 실패 시 Optional.empty() 반환.
	 */
	public Optional<String> createConnectedId(
		InstitutionType institutionType,
		String loginId,
		String loginPassword) {
		try {
			Map<String, Object> params = buildAccountParams(institutionType, loginId, loginPassword);
			EasyCodefResponse response = requestProduct(ACCOUNT_CREATE_URL, params);

			if (!isSuccess(response)) {
				log.warn("[Codef] CID 발급 실패 - institution={}, code={}, message={}",
					institutionType.getDisplayName(),
					response.getResult().getCode(),
					response.getResult().getMessage());
				return Optional.empty();
			}

			return extractConnectedId(response.getData());

		} catch (Exception e) {
			log.warn("[Codef] CID 발급 중 예외 발생 - institution={}, reason={}",
				institutionType.getDisplayName(), e.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * 기존 ConnectedId에 기관을 추가한다.
	 * 실패 시 false 반환.
	 */
	public boolean addAccount(
		String connectedId,
		InstitutionType institutionType,
		String loginId,
		String loginPassword) {
		try {
			Map<String, Object> params = buildAccountParams(institutionType, loginId, loginPassword);
			params.put("connectedId", connectedId);

			EasyCodefResponse response = requestProduct(ACCOUNT_ADD_URL, params);

			if (!isSuccess(response)) {
				log.warn("[Codef] 계정 추가 실패 - institution={}, code={}, message={}",
					institutionType.getDisplayName(),
					response.getResult().getCode(),
					response.getResult().getMessage());
				return false;
			}

			return true;

		} catch (Exception e) {
			log.warn("[Codef] 계정 추가 중 예외 발생 - institution={}, reason={}",
				institutionType.getDisplayName(), e.getMessage());
			return false;
		}
	}

	private Map<String, Object> buildAccountParams(
		InstitutionType institutionType,
		String loginId,
		String loginPassword) throws Exception {
		String encryptedPassword = RsaUtil.encryptRsa(loginPassword, easyCodefClient.getPublicKey());

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

	private EasyCodefResponse requestProduct(String url, Map<String, Object> params) {
		EasyCodefRequest request = EasyCodefRequestBuilder.builder()
			.productUrl(url)
			.parameterMap(params)
			.build();
		return easyCodefClient.requestProduct(request);
	}

	private boolean isSuccess(EasyCodefResponse response) {
		return SUCCESS_CODE.equals(response.getResult().getCode());
	}

	private Optional<String> extractConnectedId(Object data) {
		if (!(data instanceof Map<?, ?> dataMap)) {
			return Optional.empty();
		}

		Object connectedId = dataMap.get("connectedId");
		if (connectedId instanceof String id && !id.isBlank()) {
			return Optional.of(id);
		}

		return Optional.empty();
	}
}
