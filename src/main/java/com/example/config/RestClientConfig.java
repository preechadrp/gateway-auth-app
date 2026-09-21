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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.example.component.Appconfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestClientConfig {

	private final Appconfig appconfig;

	public RestClientConfig(
			Appconfig appconfig) {

		this.appconfig = appconfig;
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
		if (appconfig.getKeyStore() != null && !appconfig.getKeyStore().isBlank()
				&& appconfig.getKeyStorePassword() != null && !appconfig.getKeyStorePassword().isBlank()) {

			File keyStoreFile = new File(appconfig.getKeyStore());
			if (keyStoreFile.exists()) {

				KeyStore keyStore = KeyStore.getInstance(appconfig.getKeyStoreType()); // JKS หรือ PKCS12
				try (InputStream is = new FileInputStream(keyStoreFile)) {
					keyStore.load(is, appconfig.getKeyStorePassword().toCharArray());
				}
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(keyStore, appconfig.getKeyStorePassword().toCharArray());

				keymanager = kmf.getKeyManagers();
				log.info("Successfully initialized keystore.");
			}
		}

		// โหลด Truststore
		if (appconfig.getTrustStore() != null && !appconfig.getTrustStore().isBlank()
				&& appconfig.getTrustStorePassword() != null && !appconfig.getTrustStorePassword().isBlank()) {

			File trustStoreFile = new File(appconfig.getTrustStore());
			if (trustStoreFile.exists()) {
				KeyStore trustStore = KeyStore.getInstance(appconfig.getTrustStoreType()); // JKS หรือ PKCS12
				try (InputStream is = new FileInputStream(trustStoreFile)) {
					trustStore.load(is, appconfig.getTrustStorePassword().toCharArray());
				}
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(trustStore);

				trustManager = tmf.getTrustManagers();
				log.info("Successfully initialized truststore.");
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
		if ("true".equalsIgnoreCase(appconfig.getProxyEnabled()) && appconfig.getProxyHost() != null
				&& !appconfig.getProxyHost().isBlank() && appconfig.getProxyPort() != null && appconfig.getProxyPort() > 0) {

			ProxySelector proxySelector = ProxySelector.of(new InetSocketAddress(appconfig.getProxyHost(), appconfig.getProxyPort()));
			httpClientBuilder.proxy(proxySelector);
			log.info("Successfully initialized proxy.");

			if (appconfig.getProxyUser() != null && !appconfig.getProxyUser().isBlank()
					&& appconfig.getProxyPassword() != null && !appconfig.getProxyPassword().isBlank()) {

				Authenticator proxyAuthenticator = new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(Appconfig.getInstance().getProxyUser(), Appconfig.getInstance().getProxyPassword().toCharArray());
					}
				};

				httpClientBuilder.authenticator(proxyAuthenticator);
				log.info("Successfully initialized proxy authenticator.");
			}
		}

		HttpClient httpClient = httpClientBuilder.build();

		return new JdkClientHttpRequestFactory(httpClient);
	}

}