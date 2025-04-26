package com.zipline.service.message.dto.message.response;

import com.zipline.entity.enums.MessageTemplateCategory;
import com.zipline.entity.message.MessageTemplate;
import lombok.Getter;

@Getter
public class MessageTemplateResponseDTO {
  private Long uid;
  private String name;
  private MessageTemplateCategory category;
  private String content;

  public MessageTemplateResponseDTO(MessageTemplate messageTemplate) {
    this.uid = messageTemplate.getUid();
    this.name = messageTemplate.getName();
    this.category = messageTemplate.getCategory();
    this.content = messageTemplate.getContent();
  }
}