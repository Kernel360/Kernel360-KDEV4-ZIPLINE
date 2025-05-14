package com.zipline.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class KakaoWebClientConfig {

	@Value("${kakao.api.key}")
	private String kakaoApiKey;

	@Bean(value = "kakaoWebClient")
	public WebClient kakaoWebClient() {
		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory("https://dapi.kakao.com");
		factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

		return WebClient.builder()
			.baseUrl("https://dapi.kakao.com")
			.defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
			.uriBuilderFactory(factory)
			.build();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
