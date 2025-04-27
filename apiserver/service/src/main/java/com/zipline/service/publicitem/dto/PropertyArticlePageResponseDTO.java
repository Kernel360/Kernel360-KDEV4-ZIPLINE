package com.zipline.service.publicitem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyArticlePageResponseDTO {
    private List<PropertyArticleViewDTO> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    /**
     * Page 객체를 응답 DTO로 변환
     */
    public static PropertyArticlePageResponseDTO from(Page<PropertyArticleViewDTO> page) {
        return PropertyArticlePageResponseDTO.builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}

