package io.api.myasset.domain.approval.application;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * CODEF 가맹점 업종/이름 응답을 사용자에게 노출할 카테고리 문자열로 변환.
 * <p>
 * CODEF 의 {@code resMemberStoreType} 은 "PG일반 비인증", "인터넷상거래", "일반상품권" 등
 * 결제 중개사 수준의 추상 라벨이 붙는 경우가 많다. 이때는 {@code resMemberStoreName} 을
 * 대신 사용하여 더 구체적인 카테고리명을 제공한다.
 * <p>
 * 변환 단계:
 * <ol>
 *   <li>type 이 구체적이면 (추상 블랙리스트 밖) 그대로 사용</li>
 *   <li>type 이 추상/없음이면 name 을 사용하되:
 *     <ul>
 *       <li>법인 접두어 ("(주)", "주식회사" 등) 제거</li>
 *       <li>특수문자 → 공백 정규화 (parser 와 동일 규칙)</li>
 *       <li>알려진 법인명 → 서비스명 매핑 ("네이버파이낸셜" → "네이버페이" 등)</li>
 *     </ul>
 *   </li>
 * </ol>
 */
@Component
public class MerchantTypeResolver {

	/**
	 * storeName 으로 대체해야 하는 추상 업종 목록.
	 * {@link CardApprovalParser#sanitizeMerchantType(String)} 적용 후 형식 기준.
	 */
	private static final Set<String> ABSTRACT_TYPES = Set.of(
		"PG일반 비인증",
		"PG일반 인증",
		"인터넷상거래",
		"일반상품권",
		"전자상거래",
		"결제대행");

	/**
	 * 가맹점명 첫머리에서 제거할 법인 접두어 (긴 것부터 매칭).
	 */
	private static final List<String> LEGAL_PREFIXES = List.of(
		"주식회사",
		"유한회사",
		"(주)",
		"(유)",
		"(사)",
		"(재)");

	/**
	 * 정규화된 가맹점명에 포함되면 대체할 알려진 브랜드 매핑.
	 * 법인/원본명 → 친숙한 서비스명 (예: "네이버파이낸셜" → "네이버페이").
	 * <p>
	 * 매칭은 {@link String#contains} 로 부분 일치. 키는 sanitize 후 상태여야 함.
	 */
	private static final Map<String, String> KNOWN_BRANDS = Map.ofEntries(
		Map.entry("네이버파이낸셜", "네이버페이"),
		Map.entry("네이버페이", "네이버페이"),
		Map.entry("카카오페이", "카카오페이"),
		Map.entry("카카오커머스", "카카오쇼핑"),
		Map.entry("비바리퍼블리카", "토스페이"),
		Map.entry("토스페이먼츠", "토스페이"),
		Map.entry("쿠팡페이", "쿠팡페이"),
		Map.entry("쿠팡", "쿠팡"),
		Map.entry("우아한형제들", "배달의민족"),
		Map.entry("배달의민족", "배달의민족"),
		Map.entry("요기요", "요기요"),
		Map.entry("쿠팡이츠", "쿠팡이츠"),
		Map.entry("당근마켓", "당근"),
		Map.entry("당근페이", "당근페이"),
		Map.entry("11번가", "11번가"),
		Map.entry("지마켓", "지마켓"),
		Map.entry("옥션", "옥션"),
		Map.entry("무신사", "무신사"),
		Map.entry("컬처랜드", "컬처랜드"),
		Map.entry("해피머니", "해피머니"));

	/**
	 * 원본 type / name 에서 최종 카테고리 문자열을 도출한다.
	 * 둘 다 비어 있거나 결과가 빈 문자열이면 {@code ""} 반환 (호출자가 skip 판정).
	 */
	public String resolve(String rawType, String rawName) {
		if (rawType != null) {
			String type = CardApprovalParser.sanitizeMerchantType(rawType);
			if (!type.isEmpty() && !ABSTRACT_TYPES.contains(type)) {
				return type;
			}
		}
		return normalizeStoreName(rawName);
	}

	private String normalizeStoreName(String rawName) {
		if (rawName == null || rawName.isBlank()) {
			return "";
		}

		String name = rawName.trim();
		for (String prefix : LEGAL_PREFIXES) {
			if (name.startsWith(prefix)) {
				name = name.substring(prefix.length()).trim();
				break;
			}
		}

		String sanitized = CardApprovalParser.sanitizeMerchantType(name);
		if (sanitized.isEmpty()) {
			return "";
		}

		for (Map.Entry<String, String> entry : KNOWN_BRANDS.entrySet()) {
			if (sanitized.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return sanitized;
	}
}
