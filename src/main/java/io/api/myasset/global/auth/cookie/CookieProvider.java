package io.api.myasset.global.auth.cookie;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import io.api.myasset.global.auth.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieProvider {

	private final JwtProperties jwtProperties;

	public ResponseCookie createRefreshTokenCookie(String refreshToken) {
		return ResponseCookie.from("refresh_token", refreshToken)
			.httpOnly(true)
			.secure(false)
			.path("/")
			.maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpiry()))
			.sameSite("Lax")
			.build();
	}

	public ResponseCookie expireRefreshTokenCookie() {
		return ResponseCookie.from("refresh_token", "")
			.httpOnly(true)
			.secure(false)
			.path("/")
			.maxAge(0)
			.sameSite("Lax")
			.build();
	}
}
