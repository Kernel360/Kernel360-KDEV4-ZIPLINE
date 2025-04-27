package com.zipline.controller.publicitem;

import java.time.LocalDateTime;

import com.zipline.entity.enums.Category;
import com.zipline.service.publicitem.PropertyArticleViewService;
import com.zipline.service.publicitem.dto.PropertyArticlePageResponseDTO;
import com.zipline.service.publicitem.dto.PropertyArticleSearchDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/property-articles")
@RequiredArgsConstructor
@Tag(name = "공개 매물 목록 검색", description = "공개 매물 목록 검색 API")
public class PropertyArticleViewController {

	private final PropertyArticleViewService propertyArticleViewService;

	@Operation(summary = "매물 목록 검색", description = "매물 목록 검색 API")
	@GetMapping("/search")
	public ResponseEntity<PropertyArticlePageResponseDTO> searchPropertyArticles(
		@RequestParam(required = false) String regionCode,
		@RequestParam(required = false) String buildingName,
		@RequestParam(required = false) String buildingType,
		@RequestParam(required = false) Category category,
		@RequestParam(required = false) Long minPrice,
		@RequestParam(required = false) Long maxPrice,
		@RequestParam(required = false) Long minDeposit,
		@RequestParam(required = false) Long maxDeposit,
		@RequestParam(required = false) Long minMonthlyRent,
		@RequestParam(required = false) Long maxMonthlyRent,
		@RequestParam(required = false) Double minArea,
		@RequestParam(required = false) Double maxArea,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "desc") String sortDirection,
		@RequestParam(defaultValue = "0") Integer page,
		@RequestParam(defaultValue = "10") Integer size) {

		PropertyArticleSearchDTO searchDTO = PropertyArticleSearchDTO.fromRequestParams(
			regionCode, buildingName, buildingType, category, minPrice, maxPrice, minDeposit, maxDeposit,
			minMonthlyRent, maxMonthlyRent, minArea, maxArea, sortBy, sortDirection, page, size);

		PropertyArticlePageResponseDTO response = propertyArticleViewService.searchPropertyArticles(searchDTO);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "지역별 매물 목록 조회", description = "지역별 매물 목록 조회 API")
	@GetMapping("/region/{regionCode}")
	public ResponseEntity<PropertyArticlePageResponseDTO> getPropertyArticlesByRegion(
		@PathVariable String regionCode,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sortBy,
		@RequestParam(defaultValue = "desc") String sortDirection) {

		Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

		PropertyArticlePageResponseDTO response = propertyArticleViewService.getPropertyArticlesByRegion(regionCode,
			pageable);
		return ResponseEntity.ok(response);
	}
}

