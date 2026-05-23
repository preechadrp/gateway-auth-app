package com.example.auth.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class TestRestclientController {

	private final RestClient restClient;

	public TestRestclientController(@Qualifier("myRestClient") RestClient restClient) {
		this.restClient = restClient;
	}

	@GetMapping("/TestRestclient")
	public String test() {
		String testurl = "http://localhost:8080/auth/login";

		ResponseEntity<String> response = restClient.post()
				.uri(URI.create(testurl))
				//.body(jsonBody)
				.retrieve()
				.toEntity(String.class);

		int status = response.getStatusCode().value();
		String body = response.getBody();
		log.info("status : {}", status);
		log.info("body : {}", body);

		if (body != null) {
			return body;
		}

		return "TestRestclient Failed";
	}

}
