package com.example.config;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.security.KeyStore;
import java.security.SecureRandom;
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
import org.springframework.core.io.Resource;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	private final Resource keystoreResource;
	private final String encryptedKeystorePass;
	private final Resource truststoreResource;
	private final String encryptedTruststorePass;
	private final String proxyHost;
	private final Integer proxyPort;
	private final String proxyUser;
	private final String proxyPassword;

	public RestClientConfig(
			@Value("${my-app.secure-api.keystore-path:}") Resource keystoreResource,
			@Value("${my-app.secure-api.keystore-pass:}") String encryptedKeystorePass,
			@Value("${my-app.secure-api.truststore-path:}") Resource truststoreResource,
			@Value("${my-app.secure-api.truststore-pass:}") String encryptedTruststorePass,
			@Value("${my-app.secure-api.proxy-host:}") String proxyHost,
			@Value("${my-app.secure-api.proxy-port:0}") Integer proxyPort,
			@Value("${my-app.secure-api.proxy-user:}") String proxyUser,
			@Value("${my-app.secure-api.proxy-password:}") String proxyPassword) {

		this.keystoreResource = keystoreResource;
		this.encryptedKeystorePass = encryptedKeystorePass;
		this.truststoreResource = truststoreResource;
		this.encryptedTruststorePass = encryptedTruststorePass;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyUser = proxyUser;
		this.proxyPassword = proxyPassword;
	}

	@Bean
	RestClient restClient(RestClient.Builder builder) throws Exception {

		KeyManager[] km = null;
		TrustManager[] tm = null;

		// โหลด keystore
		if (keystoreResource != null && keystoreResource.exists()
				&& encryptedKeystorePass != null && !encryptedKeystorePass.isBlank()) {

			String keystorePass = decodePassword(encryptedKeystorePass);

			KeyStore keyStore = KeyStore.getInstance("JKS"); // หรือ PKCS12
			try (InputStream is = keystoreResource.getInputStream()) {
				keyStore.load(is, keystorePass.toCharArray());
			}
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, keystorePass.toCharArray());

			km = kmf.getKeyManagers();
			System.out.println("Loaded Keystore successfully.");
		}

		// โหลด Truststore
		if (truststoreResource != null && truststoreResource.exists()
				&& encryptedTruststorePass != null && !encryptedTruststorePass.isBlank()) {

			String truststorePass = decodePassword(encryptedTruststorePass);

			KeyStore trustStore = KeyStore.getInstance("JKS");
			try (InputStream is = truststoreResource.getInputStream()) {
				trustStore.load(is, truststorePass.toCharArray());
			}
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);

			tm = tmf.getTrustManagers();
			System.out.println("Loaded Truststore successfully.");
		}

		//สร้าง SSLContext ด้วย Java มาตรฐาน
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(km, tm, new SecureRandom());

		Builder httpClientBuilder = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.executor(Executors.newVirtualThreadPerTaskExecutor()) //ให้รองรับ virtual thread
				.sslContext(sslContext);

		// สร้าง proxy
		if (proxyHost != null && !proxyHost.isBlank() && proxyPort != null && proxyPort > 0) {
			ProxySelector proxySelector = ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort));
			httpClientBuilder.proxy(proxySelector);
			System.out.println("Proxy configured.");

			if (proxyUser != null && !proxyUser.isBlank() && proxyPassword != null && !proxyPassword.isBlank()) {

				Authenticator proxyAuthenticator = new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
					}
				};

				httpClientBuilder.authenticator(proxyAuthenticator);
				System.out.println("Proxy Authenticator configured.");
			}
		}

		HttpClient httpClient = httpClientBuilder.build();

		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

		return builder.requestFactory(requestFactory)
				.defaultHeader("Content-Type", "application/json;charset=UTF-8")
				.build();
	}

	// เมธอดสำหรับ Decode รหัสผ่าน (ใส่ Logic ของคุณได้เลย)
	private String decodePassword(String encrypted) {
		// ตัวอย่าง: ถ้าแค่ Base64
		// return new String(java.util.Base64.getDecoder().decode(encrypted));

		// หรือถ้าเป็น AES ก็ใส่ Logic decrypt ตรงนี้
		return myCustomDecryptionLogic(encrypted);
	}

	private String myCustomDecryptionLogic(String encrypted) {
		// ... โค้ดถอดรหัสของคุณ ...
		return encrypted;
	}
}