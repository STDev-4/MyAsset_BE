package io.api.myasset.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.codef.api.EasyCodefBuilder;
import io.codef.api.EasyCodefClient;
import io.codef.api.EasyCodefServiceType;

@Configuration
public class CodefConfig {

	@Value("${codef.client-id}")
	private String codefClientId;

	@Value("${codef.client-secret}")
	private String codefClientSecret;

	@Value("${codef.public-key}")
	private String codefPublicKey;

	@Bean
	public EasyCodefClient easyCodef() {
		return EasyCodefBuilder.builder()
			.serviceType(EasyCodefServiceType.DEMO)
			.clientId(codefClientId)
			.clientSecret(codefClientSecret)
			.publicKey(codefPublicKey)
			.build();
	}
}
