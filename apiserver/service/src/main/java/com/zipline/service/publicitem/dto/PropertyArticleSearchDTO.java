package com.zipline.service.publicitem.dto;

import com.zipline.entity.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyArticleSearchDTO {
    private String regionCode;
    private String buildingName;
    private String buildingType;
    private Category category;
    private Long minPrice;
    private Long maxPrice;
    private Long minDeposit;
    private Long maxDeposit;
    private Long minMonthlyRent;
    private Long maxMonthlyRent;
    private Double minArea;
    private Double maxArea;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;

    public static PropertyArticleSearchDTO fromRequestParams(
        String regionCode, String buildingName, String buildingType, Category category,
        Long minPrice, Long maxPrice, Long minDeposit, Long maxDeposit,
        Long minMonthlyRent, Long maxMonthlyRent, Double minArea, Double maxArea,
        String sortBy, String sortDirection, Integer page, Integer size) {

        return PropertyArticleSearchDTO.builder()
            .regionCode(regionCode)
            .buildingName(buildingName)
            .buildingType(buildingType)
            .category(category)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .minDeposit(minDeposit)
            .maxDeposit(maxDeposit)
            .minMonthlyRent(minMonthlyRent)
            .maxMonthlyRent(maxMonthlyRent)
            .minArea(minArea)
            .maxArea(maxArea)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .page(page)
            .size(size)
            .build();
    }
}
