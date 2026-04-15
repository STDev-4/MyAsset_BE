package io.api.myasset.domain.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InstitutionType {

	// 증권사 - clientType "A"
	YUANTA_SECURITIES("유안타증권", Category.SECURITIES, "0209", "ST", "A"),
	KB_SECURITIES("KB증권", Category.SECURITIES, "0218", "ST", "A"),
	GOLDEN_BRIDGE_SECURITIES("골든브릿지투자증권", Category.SECURITIES, "0221", "ST", "A"),
	HANYANG_SECURITIES("한양증권", Category.SECURITIES, "0222", "ST", "A"),
	LEADING_SECURITIES("리딩투자증권", Category.SECURITIES, "0223", "ST", "A"),
	BNK_SECURITIES("BNK투자증권", Category.SECURITIES, "0024", "ST", "A"),
	IBK_SECURITIES("IBK투자증권", Category.SECURITIES, "0225", "ST", "A"),
	DAOL_SECURITIES("다올투자증권", Category.SECURITIES, "0227", "ST", "A"),
	MIRAE_ASSET_SECURITIES("미래에셋증권", Category.SECURITIES, "0238", "ST", "A"),
	SAMSUNG_SECURITIES("삼성증권", Category.SECURITIES, "0240", "ST", "A"),
	KOREA_INVESTMENT_SECURITIES("한국투자증권", Category.SECURITIES, "0243", "ST", "A"),
	NH_INVESTMENT_SECURITIES("NH투자증권", Category.SECURITIES, "0247", "ST", "A"),
	NH_NAMU_SECURITIES("NH투자증권 나무", Category.SECURITIES, "1247", "ST", "A"),
	KYOBO_SECURITIES("교보증권", Category.SECURITIES, "0261", "ST", "A"),
	HI_SECURITIES("하이투자증권", Category.SECURITIES, "0262", "ST", "A"),
	HMC_SECURITIES("HMC투자증권", Category.SECURITIES, "0263", "ST", "A"),
	KIWOOM_SECURITIES("키움증권", Category.SECURITIES, "0264", "ST", "A"),
	EBEST_SECURITIES("이베스트투자증권", Category.SECURITIES, "0265", "ST", "A"),
	SK_SECURITIES("SK증권", Category.SECURITIES, "0266", "ST", "A"),
	DAISHIN_SECURITIES("대신증권", Category.SECURITIES, "0267", "ST", "A"),
	DAISHIN_CREON_SECURITIES("대신증권 크레온", Category.SECURITIES, "1267", "ST", "A"),
	HANWHA_SECURITIES("한화투자증권", Category.SECURITIES, "0269", "ST", "A"),
	HANA_SECURITIES("하나증권", Category.SECURITIES, "0270", "ST", "A"),
	SHINHAN_SECURITIES("신한금융투자", Category.SECURITIES, "0278", "ST", "A"),
	DB_SECURITIES("DB금융투자", Category.SECURITIES, "0279", "ST", "A"),
	EUGENE_SECURITIES("유진투자증권", Category.SECURITIES, "0280", "ST", "A"),
	MERITZ_SECURITIES("메리츠증권", Category.SECURITIES, "0287", "ST", "A"),
	BOOKOOK_SECURITIES("부국증권", Category.SECURITIES, "0290", "ST", "A"),
	SHINYOUNG_SECURITIES("신영증권", Category.SECURITIES, "0291", "ST", "A"),
	CAPE_SECURITIES("케이프투자증권", Category.SECURITIES, "0292", "ST", "A"),
	WOORI_SECURITIES("우리투자증권", Category.SECURITIES, "0294", "ST", "A"),
	WOORI_COMPREHENSIVE_FINANCE("우리종합금융", Category.SECURITIES, "0295", "ST", "A"),

	// 은행 - clientType "P"
	KDB_BANK("산업은행", Category.BANK, "0002", "BK", "P"),
	IBK_BANK("기업은행", Category.BANK, "0003", "BK", "P"),
	KB_BANK("국민은행", Category.BANK, "0004", "BK", "P"),
	SUHYUP_BANK("수협은행", Category.BANK, "0007", "BK", "P"),
	NH_BANK("농협은행", Category.BANK, "0011", "BK", "P"),
	WOORI_BANK("우리은행", Category.BANK, "0020", "BK", "P"),
	SC_BANK("SC은행", Category.BANK, "0023", "BK", "P"),
	CITI_BANK("씨티은행", Category.BANK, "0027", "BK", "P"),
	DAEGU_BANK("대구은행", Category.BANK, "0031", "BK", "P"),
	BUSAN_BANK("부산은행", Category.BANK, "0032", "BK", "P"),
	GWANGJU_BANK("광주은행", Category.BANK, "0034", "BK", "P"),
	JEJU_BANK("제주은행", Category.BANK, "0035", "BK", "P"),
	JEONBUK_BANK("전북은행", Category.BANK, "0037", "BK", "P"),
	KYONGNAM_BANK("경남은행", Category.BANK, "0039", "BK", "P"),
	SAEMAUL_BANK("새마을금고", Category.BANK, "0045", "BK", "P"),
	SHINHYUP_BANK("신협은행", Category.BANK, "0048", "BK", "P"),
	POST_OFFICE_BANK("우체국", Category.BANK, "0071", "BK", "P"),
	HANA_BANK("KEB하나은행", Category.BANK, "0081", "BK", "P"),
	SHINHAN_BANK("신한은행", Category.BANK, "0088", "BK", "P"),

	// 카드사 - clientType "P"
	KB_CARD("KB카드", Category.CARD, "0301", "CD", "P"),
	HYUNDAI_CARD("현대카드", Category.CARD, "0302", "CD", "P"),
	SAMSUNG_CARD("삼성카드", Category.CARD, "0303", "CD", "P"),
	NH_CARD("NH카드", Category.CARD, "0304", "CD", "P"),
	BC_CARD("BC카드", Category.CARD, "0305", "CD", "P"),
	SHINHAN_CARD("신한카드", Category.CARD, "0306", "CD", "P"),
	CITI_CARD("씨티카드", Category.CARD, "0307", "CD", "P"),
	WOORI_CARD("우리카드", Category.CARD, "0309", "CD", "P"),
	LOTTE_CARD("롯데카드", Category.CARD, "0311", "CD", "P"),
	HANA_CARD("하나카드", Category.CARD, "0313", "CD", "P"),
	JEONBUK_CARD("전북카드", Category.CARD, "0315", "CD", "P"),
	GWANGJU_CARD("광주카드", Category.CARD, "0316", "CD", "P"),
	SUHYUP_CARD("수협카드", Category.CARD, "0320", "CD", "P"),
	JEJU_CARD("제주카드", Category.CARD, "0321", "CD", "P");

	private final String displayName;
	private final Category category;
	private final String codefOrgCode;      // Codef 기관 코드
	private final String codefBusinessType; // Codef 업무 구분: BK/ST/CD
	private final String codefClientType;   // Codef 고객 구분: A(증권개인), P(은행/카드개인), B(법인)

	public enum Category {
		SECURITIES, BANK, CARD
	}
}
