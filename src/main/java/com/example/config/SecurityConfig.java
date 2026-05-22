package com.example.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Value("${jwt.secret}")
	private String secret;

	@Bean
	JwtDecoder jwtDecoder() {

		SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

		return NimbusJwtDecoder
				.withSecretKey(key)
				.build();
	}

	@Bean
	SecurityFilterChain security(
			HttpSecurity http)
			throws Exception {

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter());

		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/auth/**")
						.permitAll()

						.requestMatchers("/api/sign/**")
						.hasRole("SIGNER")

						.anyRequest()
						.authenticated())

				.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));

		return http.build();
	}
}