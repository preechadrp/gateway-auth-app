package com.example.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestClientConfig {

	private final String keystore;
	private final String keystorePassword;
	private final String keyStoreType;
	private final String truststore;
	private final String truststorePassword;
	private final String trustStoreType;
	private final String proxyEnabled;
	private final String proxyHost;
	private final Integer proxyPort;
	private final String proxyUser;
	private final String proxyPassword;

	public RestClientConfig(
			@Value("${server.ssl.key-store:}") String keystore,
			@Value("${server.ssl.key-store-password:}") String keystorePassword,
			@Value("${server.ssl.key-store-type:}") String keyStoreType,
			@Value("${server.ssl.trust-store:}") String truststore,
			@Value("${server.ssl.trust-store-password:}") String truststorePassword,
			@Value("${server.ssl.trust-store-type:}") String trustStoreType,
			@Value("${my-app.proxy.enabled:false}") String proxyEnabled,
			@Value("${my-app.proxy.host:}") String proxyHost,
			@Value("${my-app.proxy.port:}") Integer proxyPort,
			@Value("${my-app.proxy.user:}") String proxyUser,
			@Value("${my-app.proxy.password:}") String proxyPassword) {

		this.keystore = keystore;
		this.keystorePassword = keystorePassword;
		this.keyStoreType = keyStoreType;
		this.truststore = truststore;
		this.truststorePassword = truststorePassword;
		this.trustStoreType = trustStoreType;
		this.proxyEnabled = proxyEnabled;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort == null ? 0 : proxyPort;
		this.proxyUser = proxyUser;
		this.proxyPassword = proxyPassword;

		log.info("this.keystore : {}", keystore);
		log.info("this.truststore : {}", truststore);
	}

	@Bean("myRestClient")
	RestClient myRestClient() throws Exception {

		JdkClientHttpRequestFactory requestFactory = buildJdkClientHttpRequestFactory();

		return RestClient.builder()
				.requestFactory(requestFactory)
				.defaultHeader("Content-Type", "application/json;charset=UTF-8")
				.build();

	}

	private JdkClientHttpRequestFactory buildJdkClientHttpRequestFactory()
			throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {

		KeyManager[] keymanager = null;
		TrustManager[] trustManager = null;

		// โหลด keystore
		if (this.keystore != null && !this.keystore.isBlank()
				&& keystorePassword != null && !keystorePassword.isBlank()) {

			File keyStoreFile = new File(this.keystore);
			if (keyStoreFile.exists()) {

				KeyStore keyStore = KeyStore.getInstance(this.keyStoreType); // JKS หรือ PKCS12
				try (InputStream is = new FileInputStream(keyStoreFile)) {
					keyStore.load(is, this.keystorePassword.toCharArray());
				}
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(keyStore, this.keystorePassword.toCharArray());

				keymanager = kmf.getKeyManagers();
				log.info("Loaded Keystore successfully.");
			}
		}

		// โหลด Truststore
		if (this.truststore != null && !this.truststore.isBlank()
				&& this.truststorePassword != null && !this.truststorePassword.isBlank()) {

			File trustStoreFile = new File(this.truststore);
			if (trustStoreFile.exists()) {
				KeyStore trustStore = KeyStore.getInstance(this.trustStoreType); // JKS หรือ PKCS12
				try (InputStream is = new FileInputStream(trustStoreFile)) {
					trustStore.load(is, this.truststorePassword.toCharArray());
				}
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(trustStore);

				trustManager = tmf.getTrustManagers();
				log.info("Loaded Truststore successfully.");
			}

		}

		//สร้าง SSLContext ด้วย Java มาตรฐาน
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keymanager, trustManager, new SecureRandom());

		Builder httpClientBuilder = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.executor(Executors.newVirtualThreadPerTaskExecutor()) //ให้รองรับ virtual thread
				.sslContext(sslContext);

		// สร้าง proxy
		if ("true".equalsIgnoreCase(this.proxyEnabled) && this.proxyHost != null
				&& !this.proxyHost.isBlank() && this.proxyPort != null && this.proxyPort > 0) {

			ProxySelector proxySelector = ProxySelector.of(new InetSocketAddress(this.proxyHost, this.proxyPort));
			httpClientBuilder.proxy(proxySelector);
			log.info("Proxy configured.");

			if (this.proxyUser != null && !this.proxyUser.isBlank() && this.proxyPassword != null && !this.proxyPassword.isBlank()) {

				Authenticator proxyAuthenticator = new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
					}
				};

				httpClientBuilder.authenticator(proxyAuthenticator);
				log.info("Proxy Authenticator configured.");
			}
		}

		HttpClient httpClient = httpClientBuilder.build();

		return new JdkClientHttpRequestFactory(httpClient);
	}

}