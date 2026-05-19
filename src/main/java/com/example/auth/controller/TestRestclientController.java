package com.example.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/auth")
public class TestRestclientController {

	private final RestClient restClient;

	public TestRestclientController(RestClient restClient) {
		this.restClient = restClient;
	}

	@GetMapping("/TestRestclient")
	public String test() {
		String testurl = "http://localhost:8080/auth/login";

		String response = this.restClient.post()
				.uri(testurl)
				.retrieve()
				.body(String.class);

		if (response != null) {
			return response;
		}

		return "TestRestclient Failed";
	}

}
