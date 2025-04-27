package com.zipline.service.publicitem.dto;

import com.zipline.entity.enums.Category;
import com.zipline.entity.enums.Platform;
import com.zipline.entity.publicitem.PropertyArticle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyArticleViewDTO {
    private Long id;
    private String articleId;
    private String regionCode;
    private Category category;
    private String buildingName;
    private String description;
    private String buildingType;
    private Long price;
    private Long deposit;
    private Long monthlyRent;
    private Double longitude;
    private Double latitude;
    private Double supplyArea;
    private Double exclusiveArea;
    private Platform platform;
    private String platformUrl;

    /**
     * PropertyArticle 엔티티를 DTO로 변환
     */
    public static PropertyArticleViewDTO from(PropertyArticle article) {
        return PropertyArticleViewDTO.builder()
                .id(article.getId())
                .articleId(article.getArticleId())
                .regionCode(article.getRegionCode())
                .category(article.getCategory())
                .buildingName(article.getBuildingName())
                .description(article.getDescription())
                .buildingType(article.getBuildingType())
                .price(article.getPrice())
                .deposit(article.getDeposit())
                .monthlyRent(article.getMonthlyRent())
                .longitude(article.getLongitude())
                .latitude(article.getLatitude())
                .supplyArea(article.getSupplyArea())
                .exclusiveArea(article.getExclusiveArea())
                .platform(article.getPlatform())
                .platformUrl(article.getPlatformUrl())
                .build();
    }

    /**
     * PropertyArticle 엔티티 리스트를 DTO 리스트로 변환
     */
    public static List<PropertyArticleViewDTO> fromList(List<PropertyArticle> articles) {
        return articles.stream()
                .map(PropertyArticleViewDTO::from)
                .collect(Collectors.toList());
    }
}
