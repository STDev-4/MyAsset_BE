package io.api.myasset.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	private static final String bearerSchemeName = "bearerAuth";

	@Bean
	public OpenAPI openAPI() {
		SecurityScheme securityScheme = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("Authorization");

		SecurityRequirement securityRequirement = new SecurityRequirement()
			.addList(bearerSchemeName);

		return new OpenAPI()
			.info(new Info()
				.title("MyAsset API")
				.version("v1")
				.description("MyAsset Swagger API Docs"))
			.components(new Components()
				.addSecuritySchemes(bearerSchemeName, securityScheme))
			.addSecurityItem(securityRequirement);
	}
}
