package com.example.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService {

	@Value("${jwt.secret}")
	private String secret;

	public String generateToken(
			String username,
			List<String> roles)
			throws JOSEException {

		log.info("secret : {}", secret);

		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.subject(username)
				.claim("roles", roles)
				.issueTime(new Date())
				.expirationTime(new Date(System.currentTimeMillis() + 3600000))
				.build();

		SignedJWT jwt = new SignedJWT(
				new JWSHeader(JWSAlgorithm.HS256),
				claims);

		MACSigner signer = new MACSigner(
				new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

		jwt.sign(signer);

		return jwt.serialize();
	}
}