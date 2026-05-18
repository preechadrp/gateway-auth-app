package com.example.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.service.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final JwtService jwtService;

	public AuthController(
			JwtService jwtService) {

		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public Map<String, String> login()
			throws Exception {

		String token = jwtService.generateToken(
				"john",
				List.of("SIGNER", "CA"));

		return Map.of(
				"token",
				token);
	}
}