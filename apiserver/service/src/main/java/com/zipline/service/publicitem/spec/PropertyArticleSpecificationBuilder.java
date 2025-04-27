package com.zipline.service.publicitem.spec;

import com.zipline.entity.publicitem.PropertyArticle;
import com.zipline.repository.publicitem.PropertyArticleViewSpecification;
import com.zipline.repository.region.RegionRepository;
import com.zipline.service.publicitem.dto.PropertyArticleSearchDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 매물 검색 조건(Specification)을 생성하는 빌더 클래스
 */
@RequiredArgsConstructor
@Component
public class PropertyArticleSpecificationBuilder {

    private final RegionRepository regionRepository;

    /**
     * 검색 DTO로부터 매물 검색 조건 Specification 생성
     */
    public Specification<PropertyArticle> buildSpecification(PropertyArticleSearchDTO searchDTO) {
        Specification<PropertyArticle> spec = Specification.where(null);

        spec = addLocationSpecifications(spec, searchDTO);
        spec = addBuildingSpecifications(spec, searchDTO);
        spec = addPriceSpecifications(spec, searchDTO);
        spec = addAreaSpecifications(spec, searchDTO);
        spec = addDateSpecifications(spec, searchDTO);

        return spec;
    }

    /**
     * 위치 관련 검색 조건 추가
     */
    private Specification<PropertyArticle> addLocationSpecifications(
            Specification<PropertyArticle> spec, PropertyArticleSearchDTO searchDTO) {

        if (searchDTO.getRegionCode() != null) {
            spec = spec.and(PropertyArticleViewSpecification.hasRegionCode(searchDTO.getRegionCode()));
        }

        return spec;
    }

    /**
     * 건물 관련 검색 조건 추가
     */
    private Specification<PropertyArticle> addBuildingSpecifications(
            Specification<PropertyArticle> spec, PropertyArticleSearchDTO searchDTO) {

        if (searchDTO.getBuildingName() != null) {
            spec = spec.and(PropertyArticleViewSpecification.buildingNameContains(searchDTO.getBuildingName()));
        }

        if (searchDTO.getCategory() != null) {
            spec = spec.and(PropertyArticleViewSpecification.hasCategory(searchDTO.getCategory()));
        }

        if (searchDTO.getBuildingType() != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("buildingType"), searchDTO.getBuildingType()));
        }

        return spec;
    }

    /**
     * 가격 관련 검색 조건 추가
     */
    private Specification<PropertyArticle> addPriceSpecifications(
            Specification<PropertyArticle> spec, PropertyArticleSearchDTO searchDTO) {

        if (searchDTO.getMinPrice() != null || searchDTO.getMaxPrice() != null) {
            spec = spec.and(PropertyArticleViewSpecification.priceBetween(
                    searchDTO.getMinPrice(), searchDTO.getMaxPrice()));
        }

        if (searchDTO.getMinDeposit() != null || searchDTO.getMaxDeposit() != null) {
            spec = spec.and(PropertyArticleViewSpecification.depositBetween(
                    searchDTO.getMinDeposit(), searchDTO.getMaxDeposit()));
        }

        if (searchDTO.getMinMonthlyRent() != null || searchDTO.getMaxMonthlyRent() != null) {
            spec = spec.and(PropertyArticleViewSpecification.monthlyRentBetween(
                    searchDTO.getMinMonthlyRent(), searchDTO.getMaxMonthlyRent()));
        }

        return spec;
    }

    /**
     * 면적 관련 검색 조건 추가
     */
    private Specification<PropertyArticle> addAreaSpecifications(
            Specification<PropertyArticle> spec, PropertyArticleSearchDTO searchDTO) {

        if (searchDTO.getMinArea() != null || searchDTO.getMaxArea() != null) {
            spec = spec.and(PropertyArticleViewSpecification.exclusiveAreaBetween(
                    searchDTO.getMinArea(), searchDTO.getMaxArea()));
        }

        return spec;
    }

    /**
     * 날짜 관련 검색 조건 추가
     */
    private Specification<PropertyArticle> addDateSpecifications(
            Specification<PropertyArticle> spec, PropertyArticleSearchDTO searchDTO) {

        if (searchDTO.getStartDate() != null || searchDTO.getEndDate() != null) {
            spec = spec.and(PropertyArticleViewSpecification.createdAtBetween(
                    searchDTO.getStartDate(), searchDTO.getEndDate()));
        }

        return spec;
    }
}