package io.api.myasset.global.auth.dto;

import java.time.LocalDate;
import java.util.List;

import io.api.myasset.domain.user.domain.CodefAccount;
import io.api.myasset.domain.user.domain.InstitutionType;
import io.api.myasset.domain.user.domain.User;

public record SignupResponse(
	Long userId,
	String loginId,
	String email,
	String nickname,
	LocalDate birthDate,
	List<InstitutionType> linkedSecurities,
	List<InstitutionType> linkedBanks,
	List<InstitutionType> linkedCards
) {
	public static SignupResponse from(User user) {
		return new SignupResponse(
			user.getId(),
			user.getLoginId(),
			user.getEmail(),
			user.getNickname(),
			user.getBirthDate(),
			user.getSecuritiesAccounts().stream().map(CodefAccount::getInstitutionType).toList(),
			user.getBankAccounts().stream().map(CodefAccount::getInstitutionType).toList(),
			user.getCardAccounts().stream().map(CodefAccount::getInstitutionType).toList()
		);
	}
}
