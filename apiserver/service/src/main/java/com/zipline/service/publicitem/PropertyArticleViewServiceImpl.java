package com.zipline.service.publicitem;

import com.zipline.entity.publicitem.PropertyArticle;
import com.zipline.global.exception.publicitem.PublicItemException;
import com.zipline.global.exception.publicitem.errorcode.PublicItemErrorCode;
import com.zipline.repository.publicitem.PropertyArticleViewRepository;
import com.zipline.service.publicitem.dto.PropertyArticlePageResponseDTO;
import com.zipline.service.publicitem.dto.PropertyArticleSearchDTO;
import com.zipline.service.publicitem.dto.PropertyArticleViewDTO;
import com.zipline.service.publicitem.spec.PropertyArticleSpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyArticleViewServiceImpl implements PropertyArticleViewService {

	private static final String DEFAULT_SORT_FIELD = "createdAt";
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 10;
	private static final String ASC = "asc";

	private final PropertyArticleViewRepository propertyArticleViewRepository;
	private final PropertyArticleSpecificationBuilder specificationBuilder;

	/**
	 * 매물 목록을 검색 조건에 따라 페이징하여 조회
	 *
	 * @param searchDTO 검색 조건
	 * @return 페이징된 매물 목록
	 * @todo 추후 Region Repository 와 specificationBuilder 활용해서 부모 지역코드로도 검색 가능하게 변경
	 */
	@Override
	@Transactional(readOnly = true)
	public PropertyArticlePageResponseDTO searchPropertyArticles(PropertyArticleSearchDTO searchDTO) {
		try {
			validateSearchParameters(searchDTO);

			Pageable pageable = createPageable(searchDTO);
			Specification<PropertyArticle> spec = specificationBuilder.buildSpecification(searchDTO);

			log.info("매물 검색 실행: 페이지={}, 사이즈={}, 정렬={} {}",
					pageable.getPageNumber(), pageable.getPageSize(),
					pageable.getSort().iterator().next().getProperty(),
					pageable.getSort().iterator().next().getDirection());

			Page<PropertyArticle> articlePage = propertyArticleViewRepository.findAll(spec, pageable);

			log.info("매물 검색 결과: 총 {}개 항목, 총 {}페이지",
					articlePage.getTotalElements(), articlePage.getTotalPages());

			return convertToResponseDTO(articlePage);
		} catch (PublicItemException e) {
			log.error("매물 검색 파라미터 오류: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("매물 검색 중 오류 발생: {}", e.getMessage(), e);
			throw new PublicItemException(PublicItemErrorCode.PUBLIC_ITEM_ERROR);
		}
	}

	/**
	 * 지역 코드로 매물 목록 조회
	 *
	 * @param regionCode 지역 코드
	 * @param pageable 페이징 정보
	 * @return 페이징된 매물 목록
	 */
	@Override
	@Transactional(readOnly = true)
	public PropertyArticlePageResponseDTO getPropertyArticlesByRegion(String regionCode, Pageable pageable) {
		try {
			log.info("지역 코드 {} 매물 조회 시작", regionCode);
			Page<PropertyArticle> articlePage = propertyArticleViewRepository.findByRegionCode(regionCode, pageable);
			log.info("지역 코드 {} 매물 조회 결과: 총 {}개 항목", regionCode, articlePage.getTotalElements());

			return convertToResponseDTO(articlePage);
		} catch (Exception e) {
			log.error("지역 코드 {} 매물 조회 중 오류 발생: {}", regionCode, e.getMessage(), e);
			throw new PublicItemException(PublicItemErrorCode.PUBLIC_ITEM_ERROR);
		}
	}

	/**
	 * 검색 파라미터 유효성 검증
	 */
	private void validateSearchParameters(PropertyArticleSearchDTO searchDTO) {
		validatePriceRange(searchDTO.getMinPrice(), searchDTO.getMaxPrice(),
				PublicItemErrorCode.INVALID_PRICE_RANGE);

		validatePriceRange(searchDTO.getMinDeposit(), searchDTO.getMaxDeposit(),
				PublicItemErrorCode.INVALID_DEPOSIT_RANGE);

		validatePriceRange(searchDTO.getMinMonthlyRent(), searchDTO.getMaxMonthlyRent(),
				PublicItemErrorCode.INVALID_MONTHLY_RENT_RANGE);

		validatePriceRange(searchDTO.getMinArea(), searchDTO.getMaxArea(),
				PublicItemErrorCode.INVALID_AREA_RANGE);
	}

	/**
	 * 범위 값의 유효성 검증
	 */
	private void validatePriceRange(Number min, Number max, PublicItemErrorCode errorCode) {
		if (min != null && max != null && min.doubleValue() > max.doubleValue()) {
			throw new PublicItemException(errorCode);
		}
	}

	/**
	 * 페이징 정보 생성
	 */
	private Pageable createPageable(PropertyArticleSearchDTO searchDTO) {
		Sort.Direction direction = searchDTO.getSortDirection() != null &&
				searchDTO.getSortDirection().equalsIgnoreCase(ASC) ?
				Sort.Direction.ASC : Sort.Direction.DESC;

		String sortBy = searchDTO.getSortBy() != null && !searchDTO.getSortBy().isEmpty() ?
				searchDTO.getSortBy() : DEFAULT_SORT_FIELD;

		int page = searchDTO.getPage() != null ? searchDTO.getPage() : DEFAULT_PAGE;
		int size = searchDTO.getSize() != null ? searchDTO.getSize() : DEFAULT_SIZE;

		return PageRequest.of(page, size, Sort.by(direction, sortBy));
	}

	/**
	 * 엔티티 페이지를 DTO 페이지로 변환
	 */
	private PropertyArticlePageResponseDTO convertToResponseDTO(Page<PropertyArticle> articlePage) {
		Page<PropertyArticleViewDTO> dtoPage = articlePage.map(PropertyArticleViewDTO::from);
		return PropertyArticlePageResponseDTO.from(dtoPage);
	}
}