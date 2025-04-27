package com.zipline.service.publicitem;

import com.zipline.service.publicitem.dto.PropertyArticlePageResponseDTO;
import com.zipline.service.publicitem.dto.PropertyArticleSearchDTO;
import org.springframework.data.domain.Pageable;


public interface PropertyArticleViewService {

	/**
	 * 검색 조건에 따른 매물 목록 조회
	 *
	 * @param searchDTO 검색 조건 DTO
	 * @return 페이징된 매물 목록 응답 DTO
	 */
	PropertyArticlePageResponseDTO searchPropertyArticles(PropertyArticleSearchDTO searchDTO);

	/**
	 * 지역 코드에 따른 매물 목록 조회
	 *
	 * @param regionCode 지역 코드
	 * @param pageable 페이징 정보
	 * @return 페이징된 매물 목록 응답 DTO
	 */
	PropertyArticlePageResponseDTO getPropertyArticlesByRegion(String regionCode, Pageable pageable);
}

