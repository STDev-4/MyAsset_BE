package io.api.myasset.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import io.api.myasset.global.auth.jwt.JwtAuthenticationFilter;
import io.api.myasset.global.exception.ApiExceptionHandlingFilter;
import io.api.myasset.global.exception.error.GlobalError;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ApiExceptionHandlingFilter apiExceptionHandlingFilter;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ErrorResponseWriter errorResponseWriter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * CORS 설정.
	 * <p>
	 * credentials: include 를 쓰는 FE 와 호환되도록 정확한 origin 을 명시 (wildcard 불가).
	 * Authorization 헤더와 쿠키 모두 허용. 운영 배포 시 실제 FE origin 을 환경변수로 주입해야 한다.
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(
			"https://main.d438ryzdewhne.amplifyapp.com"));
		config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Authorization"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/refresh", "/actuator/health",
					"/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
				.permitAll()
				.requestMatchers("/api/auth/logout").authenticated()
				.anyRequest().authenticated())
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, ex) -> errorResponseWriter.write(response,
					HttpStatus.UNAUTHORIZED, GlobalError.INVALID_TOKEN)))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(apiExceptionHandlingFilter, JwtAuthenticationFilter.class);
		return http.build();
	}
}
