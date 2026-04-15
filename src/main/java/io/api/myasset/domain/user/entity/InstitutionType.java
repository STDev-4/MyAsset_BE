package io.api.myasset.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InstitutionType {

	// 증권사 - clientType "A"
	INVEST_SAMSUNG("삼성증권", Category.INVEST, "0240", "ST", "A"),
	INVEST_NH_NAMU("NH투자증권 나무", Category.INVEST, "1247", "ST", "A"),
	INVEST_MIRAE_ASSET("미래에셋증권", Category.INVEST, "0238", "ST", "A"),
	INVEST_NH("NH투자증권", Category.INVEST, "0247", "ST", "A"),
	INVEST_SHINHAN("신한금융투자", Category.INVEST, "0278", "ST", "A"),
	INVEST_SK("SK증권", Category.INVEST, "0266", "ST", "A"),

	// 은행 - clientType "P"
	BANK_KB("국민은행", Category.BANK, "0004", "BK", "P"),
	BANK_SHINHAN("신한은행", Category.BANK, "0088", "BK", "P"),
	BANK_WOORI("우리은행", Category.BANK, "0020", "BK", "P"),
	BANK_HANA("KEB하나은행", Category.BANK, "0081", "BK", "P"),
	BANK_NH("농협은행", Category.BANK, "0011", "BK", "P"),
	BANK_IBK("기업은행", Category.BANK, "0003", "BK", "P"),

	// 카드사 - clientType "P"
	CARD_KB("KB카드", Category.CARD, "0301", "CD", "P"),
	CARD_SHINHAN("신한카드", Category.CARD, "0306", "CD", "P"),
	CARD_WOORI("우리카드", Category.CARD, "0309", "CD", "P"),
	CARD_HANA("하나카드", Category.CARD, "0313", "CD", "P"),
	CARD_NH("NH카드", Category.CARD, "0304", "CD", "P"),
	CARD_BC("BC카드", Category.CARD, "0305", "CD", "P");

	private final String displayName;
	private final Category category;
	private final String codefOrgCode;      // Codef 기관 코드
	private final String codefBusinessType; // Codef 업무 구분: BK/ST/CD
	private final String codefClientType;   // Codef 고객 구분: A(증권개인), P(은행/카드개인), B(법인)

	public enum Category {
		INVEST, BANK, CARD
	}
}
