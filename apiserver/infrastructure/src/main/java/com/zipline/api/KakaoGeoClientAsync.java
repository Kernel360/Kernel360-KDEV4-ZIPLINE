package com.zipline.api;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class KakaoGeoClientAsync {

	@Qualifier(value = "kakaoWebClient")
	private final WebClient kakaoWebClient;

	public Mono<KakaoGeocodeResponseDTO> getCoordinatesByAddressAsync(String address) {
		return kakaoWebClient
			.get()
			.uri(uriBuilder ->
				uriBuilder
					.path("/v2/local/search/address.json")
					.queryParam("query", UriUtils.encode(address, StandardCharsets.UTF_8))
					.build())
			.retrieve()
			.bodyToMono(KakaoGeocodeResponseDTO.class);
	}
}
