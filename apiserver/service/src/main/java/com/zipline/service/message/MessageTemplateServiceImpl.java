package com.zipline.service.message;

import com.zipline.entity.enums.MessageTemplateCategory;
import com.zipline.entity.message.MessageTemplate;
import com.zipline.entity.user.User;
import com.zipline.global.exception.message.MessageTemplateException;
import com.zipline.global.exception.message.errorcode.MessageTemplateErrorCode;
import com.zipline.global.exception.user.UserException;
import com.zipline.global.exception.user.errorcode.UserErrorCode;
import com.zipline.repository.message.MessageTemplateRepository;
import com.zipline.repository.user.UserRepository;
import com.zipline.service.message.dto.message.request.MessageTemplateRequestDTO;
import com.zipline.service.message.dto.message.response.MessageTemplateResponseDTO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTemplateServiceImpl implements MessageTemplateService {

	private final MessageTemplateRepository messageTemplateRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public void createMessageTemplate(MessageTemplateRequestDTO requestDTO, Long userUid) {
		if (requestDTO.getCategory().equals(MessageTemplateCategory.BIRTHDAY)
			|| requestDTO.getCategory().equals(MessageTemplateCategory.EXPIRED_NOTI)) {
			messageTemplateRepository.findByCategoryAndUserUidAndDeletedAtIsNull(requestDTO.getCategory(),
				userUid).ifPresent(template -> {
				throw new MessageTemplateException(MessageTemplateErrorCode.DUPLICATE_TEMPLATE_CATEGORY);
			});

			User user = userRepository.findById(userUid)
				.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

			LocalDateTime now = LocalDateTime.now();

			MessageTemplate messageTemplate = MessageTemplate.builder()
				.name(requestDTO.getName())
				.category(requestDTO.getCategory())
				.content(requestDTO.getContent())
				.user(user)
				.createdAt(now)
				.updatedAt(now)
				.build();

			messageTemplateRepository.save(messageTemplate);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<MessageTemplateResponseDTO> getMessageTemplateList(Long userUid) {
		List<MessageTemplate> messageTemplateList = messageTemplateRepository.findByUserUid(userUid);
		return messageTemplateList.stream()
				.map(MessageTemplateResponseDTO::new)
				.toList();
	}
}