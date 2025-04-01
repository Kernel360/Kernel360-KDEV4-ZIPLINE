package com.zipline.controller;

import com.zipline.dto.SendMessageRequestDto;
import com.zipline.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/message")
public class MessageController {

  private final MessageService messageService;

  public MessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  @PostMapping("/send")
  public Mono<ResponseEntity<String>> sendMessage(
      @RequestBody SendMessageRequestDto[] requestBody) {

    return messageService.sendMessage(requestBody)
        .map(ResponseEntity::ok)
        .onErrorResume(e -> {
          System.err.println("Error occurred while sending message: " + e.getMessage());

          // 에러 발생 시 처리
          return Mono.just(
              ResponseEntity.status(500).body(e.toString()));
        });
  }
}