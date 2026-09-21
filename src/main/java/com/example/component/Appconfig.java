package com.example.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Getter
public class Appconfig {

	private static Appconfig instance;

	@Value("${server.ssl.key-store:}")
	private String keyStore;

	@Value("${server.ssl.key-store-password:}")
	private String keyStorePassword;

	@Value("${server.ssl.key-store-type:}")
	private String keyStoreType;

	@Value("${server.ssl.trust-store:}")
	private String trustStore;

	@Value("${server.ssl.trust-store-password:}")
	private String trustStorePassword;

	@Value("${server.ssl.trust-store-type:}")
	private String trustStoreType;

	@Value("${my-app.proxy.enabled:false}")
	private String proxyEnabled;

	@Value("${my-app.proxy.host:}")
	private String proxyHost;

	@Value("${my-app.proxy.port:}")
	private Integer proxyPort;

	@Value("${my-app.proxy.user:}")
	private String proxyUser;

	@Value("${my-app.proxy.password:}")
	private String proxyPassword;

	@Value("${my-app.jwt.secret}")
	private	String secret;

	@PostConstruct
	public void init() {
		instance = this;

		log.info("this.keyStore : {}", this.keyStore);
		log.info("this.trustStore : {}", this.trustStore);
		log.info("this.proxyEnabled : {}", this.proxyEnabled);
		log.info("this.proxyHost : {}", this.proxyHost);
		log.info("this.proxyPort : {}", this.proxyPort);
		log.info("this.proxyUser : {}", this.proxyUser);
		log.info("this.secret : {}", this.secret);
	}

	public static Appconfig getInstance() {
		return instance;
	}
}
