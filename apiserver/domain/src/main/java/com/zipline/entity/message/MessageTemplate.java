package com.zipline.entity.message;

import com.zipline.entity.enums.MessageTemplateCategory;
import com.zipline.entity.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "message_templates")
@Entity
@Getter
public class MessageTemplate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long uid;

  private String name;
  private MessageTemplateCategory category;
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;


  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  @Builder
  public MessageTemplate(String name , MessageTemplateCategory category , String content,
    User user, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
    this.name = name;
    this.category = category;
    this.content = content;
    this.user = user;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.deletedAt = deletedAt;
  }
}