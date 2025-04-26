package com.zipline.service.message;

import com.zipline.service.message.dto.message.request.MessageTemplateRequestDTO;
import com.zipline.service.message.dto.message.response.MessageTemplateResponseDTO;
import java.util.List;

public interface MessageTemplateService {
  void createMessageTemplate(MessageTemplateRequestDTO requestDTO, Long userUid);
  List<MessageTemplateResponseDTO> getMessageTemplateList(Long userUid);
}