package io.api.myasset.global.config;

import io.api.myasset.global.s3.S3Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class S3Config {

	@Bean
	public DefaultCredentialsProvider defaultCredentialsProvider() {
		return DefaultCredentialsProvider.create();
	}

	@Bean
	public S3Client s3Client(S3Properties props, DefaultCredentialsProvider cp) {
		return S3Client.builder()
			.region(Region.of(props.region()))
			.credentialsProvider(cp)
			.build();
	}
}
