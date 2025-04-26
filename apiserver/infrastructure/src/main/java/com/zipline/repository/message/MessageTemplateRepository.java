package com.zipline.repository.message;

import com.zipline.entity.enums.MessageTemplateCategory;
import com.zipline.entity.message.MessageTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageTemplateRepository  extends JpaRepository<MessageTemplate, Long> {
  Optional<MessageTemplate> findByCategoryAndUserUidAndDeletedAtIsNull(MessageTemplateCategory category, Long userUid);
  List<MessageTemplate> findByUserUid(Long userUid);
}