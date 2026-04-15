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

	private final JwtProperties jwtProperties;

	public String generateAccessToken(User user) {
		Date now = new Date();
		return Jwts.builder()
			.subject(String.valueOf(user.getId()))
			.claim("userId", user.getId())
			.issuedAt(now)
			.expiration(new Date(now.getTime() + jwtProperties.getAccessTokenExpiry()))
			.signWith(getSigningKey())
			.compact();
	}

	public String generateRefreshToken(User user) {
		Date now = new Date();
		return Jwts.builder()
			.subject(String.valueOf(user.getId()))
			.claim("userId", user.getId())
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

	public Long getUserIdFromToken(String token) {
		return parseClaims(token).get("userId", Long.class);
	}

	public Claims parseClaims(String token) {
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
