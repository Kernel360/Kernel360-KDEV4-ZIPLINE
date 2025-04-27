package com.zipline.repository.publicitem;

import com.zipline.entity.enums.Category;
import com.zipline.entity.publicitem.PropertyArticle;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class PropertyArticleViewSpecification {

    /**
     * 지역 코드로 검색하는 명세
     */
    public static Specification<PropertyArticle> hasRegionCode(String regionCode) {
        return (root, query, criteriaBuilder) -> 
            regionCode == null ? null : criteriaBuilder.equal(root.get("regionCode"), regionCode);
    }

    /**
     * 건물 이름으로 검색하는 명세
     */
    public static Specification<PropertyArticle> buildingNameContains(String buildingName) {
        return (root, query, criteriaBuilder) ->
            buildingName == null ? null : criteriaBuilder.like(root.get("buildingName"), "%" + buildingName + "%");
    }

    /**
     * 카테고리로 검색하는 명세
     */
    public static Specification<PropertyArticle> hasCategory(Category category) {
        return (root, query, criteriaBuilder) ->
            category == null ? null : criteriaBuilder.equal(root.get("category"), category);
    }

    /**
     * 가격 범위로 검색하는 명세 (매매)
     */
    public static Specification<PropertyArticle> priceBetween(Long minPrice, Long maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) return null;
            if (minPrice == null) return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            if (maxPrice == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
        };
    }

    /**
     * 보증금 범위로 검색하는 명세 (전세, 월세)
     */
    public static Specification<PropertyArticle> depositBetween(Long minDeposit, Long maxDeposit) {
        return (root, query, criteriaBuilder) -> {
            if (minDeposit == null && maxDeposit == null) return null;
            if (minDeposit == null) return criteriaBuilder.lessThanOrEqualTo(root.get("deposit"), maxDeposit);
            if (maxDeposit == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("deposit"), minDeposit);
            return criteriaBuilder.between(root.get("deposit"), minDeposit, maxDeposit);
        };
    }

    /**
     * 월세 범위로 검색하는 명세 (월세)
     */
    public static Specification<PropertyArticle> monthlyRentBetween(Long minRent, Long maxRent) {
        return (root, query, criteriaBuilder) -> {
            if (minRent == null && maxRent == null) return null;
            if (minRent == null) return criteriaBuilder.lessThanOrEqualTo(root.get("monthlyRent"), maxRent);
            if (maxRent == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("monthlyRent"), minRent);
            return criteriaBuilder.between(root.get("monthlyRent"), minRent, maxRent);
        };
    }

    /**
     * 면적 범위로 검색하는 명세
     */
    public static Specification<PropertyArticle> exclusiveAreaBetween(Double minArea, Double maxArea) {
        return (root, query, criteriaBuilder) -> {
            if (minArea == null && maxArea == null) return null;
            if (minArea == null) return criteriaBuilder.lessThanOrEqualTo(root.get("exclusiveArea"), maxArea);
            if (maxArea == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("exclusiveArea"), minArea);
            return criteriaBuilder.between(root.get("exclusiveArea"), minArea, maxArea);
        };
    }

    /**
     * 등록일 범위로 검색하는 명세
     */
    public static Specification<PropertyArticle> createdAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) return null;
            if (startDate == null) return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
            if (endDate == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
        };
    }
}
