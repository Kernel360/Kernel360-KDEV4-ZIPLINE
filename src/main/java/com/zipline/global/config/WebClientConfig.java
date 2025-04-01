package com.zipline.global.config;

import com.zipline.service.SignatureService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Value("${sms.api-key}")
  String apiKey;

  @Bean
  public WebClient smsWebClient(SignatureService signatureService) {
    Map<String, String> signatureResult = signatureService.generateSignature()
        .block();

    if (signatureResult == null || signatureResult.isEmpty()) {
      throw new IllegalArgumentException("유효하지 않은 signature 값 입니다.");
    }

    String authorizationHeader = String.format(
        "HMAC-SHA256 apiKey=%s, date=%s, salt=%s, signature=%s",
        apiKey, signatureResult.get("time"), signatureResult.get("salt"),
        signatureResult.get("hash")
    );

    return WebClient.builder()
        .baseUrl("https://api.solapi.com/messages/v4")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeader) // Adding Authorization here
        .build();
  }
}