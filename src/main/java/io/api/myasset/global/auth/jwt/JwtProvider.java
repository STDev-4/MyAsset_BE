package io.api.myasset.global.auth.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.api.myasset.domain.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	private static final String CLAIM_USER_ID = "userId";
	private static final String CLAIM_TOKEN_TYPE = "type";
	private static final String CLAIM_CODEF_LINKED = "codefLinked";
	private static final String TYPE_ACCESS = "access";
	private static final String TYPE_REFRESH = "refresh";

	private final JwtProperties jwtProperties;

	public String generateAccessToken(User user) {
		Date now = new Date();
		return Jwts.builder()
			.claim(CLAIM_USER_ID, user.getId())
			.claim(CLAIM_TOKEN_TYPE, TYPE_ACCESS)
			.claim(CLAIM_CODEF_LINKED, user.hasConnectedId())
			.issuedAt(now)
			.expiration(new Date(now.getTime() + jwtProperties.getAccessTokenExpiry()))
			.signWith(getSigningKey())
			.compact();
	}

	public String generateRefreshToken(User user) {
		Date now = new Date();
		return Jwts.builder()
			.claim(CLAIM_USER_ID, user.getId())
			.claim(CLAIM_TOKEN_TYPE, TYPE_REFRESH)
			.claim(CLAIM_CODEF_LINKED, user.hasConnectedId())
			.issuedAt(now)
			.expiration(new Date(now.getTime() + jwtProperties.getRefreshTokenExpiry()))
			.signWith(getSigningKey())
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isRefreshToken(String token) {
		try {
			return TYPE_REFRESH.equals(parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class));
		} catch (Exception e) {
			return false;
		}
	}

	public Long getUserIdFromToken(String token) {
		return parseClaims(token).get(CLAIM_USER_ID, Long.class);
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSigningKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
	}
}
