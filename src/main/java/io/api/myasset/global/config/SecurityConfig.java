package io.api.myasset.global.config;

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

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/users/signup", "/api/auth/login", "/api/auth/refresh").permitAll()
				.requestMatchers("/api/auth/logout").authenticated()
				.anyRequest().authenticated()
			)
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, ex) ->
					errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, GlobalError.INVALID_TOKEN)
				)
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(apiExceptionHandlingFilter, JwtAuthenticationFilter.class);
		return http.build();
	}
}
