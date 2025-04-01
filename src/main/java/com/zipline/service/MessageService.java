package com.zipline.service;

import com.zipline.dto.SendMessageRequestDto;
import com.zipline.util.SignatureUtil;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class MessageService {

  private final WebClient webClient;

  public MessageService(WebClient webClient, SignatureUtil signatureUtil) {
    this.webClient = webClient;
  }

  public Mono<String> sendMessage(SendMessageRequestDto[] request) {

    Map<String, Object> wrappedRequest = Map.of("messages", request);

    return webClient.post()
        .uri("/send-many/detail")
        .bodyValue(wrappedRequest)
        .retrieve()
        .onStatus(
            HttpStatusCode::is4xxClientError,
            clientResponse -> clientResponse.bodyToMono(String.class).map(body -> {
              throw new RuntimeException(body);
            })
        ).bodyToMono(String.class);
  }
}