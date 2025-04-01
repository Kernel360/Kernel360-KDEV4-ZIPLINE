package com.zipline.dto;

import lombok.Data;

@Data
public class SendMessageRequestDto {

  private String from;
  private String to;
  private String text;
}