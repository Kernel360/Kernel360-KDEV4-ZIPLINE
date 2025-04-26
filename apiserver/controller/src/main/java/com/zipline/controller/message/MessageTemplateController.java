package com.zipline.controller.message;

import com.zipline.service.message.dto.message.request.MessageTemplateRequestDTO;
import com.zipline.global.response.ApiResponse;
import com.zipline.service.message.MessageTemplateService;
import com.zipline.service.message.dto.message.response.MessageTemplateResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "문자", description = "메시지 / 메시지 템플릿 관련 API")
@RestController
@RequestMapping("/api/v1/templates")
public class MessageTemplateController {

  private final MessageTemplateService messageTemplateService;

  public MessageTemplateController(MessageTemplateService messageTemplateService) {
    this.messageTemplateService = messageTemplateService;
  }

  @PostMapping("")
  public ResponseEntity<ApiResponse<Void>> createMessageTemplate(@RequestBody MessageTemplateRequestDTO request, Principal principal) {
    messageTemplateService.createMessageTemplate(request, Long.parseLong(principal.getName()));
    ApiResponse<Void> response = ApiResponse.create("문자 템플릿 등록 성공");
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("")
  public ResponseEntity<ApiResponse<List<MessageTemplateResponseDTO>>> getMessageTemplate(Principal principal) {
    List<MessageTemplateResponseDTO> messageTemplateList =messageTemplateService.getMessageTemplateList(Long.parseLong(principal.getName()));
    ApiResponse<List<MessageTemplateResponseDTO>> response = ApiResponse.ok("문자 템플릿 목록 조회 성공", messageTemplateList);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}