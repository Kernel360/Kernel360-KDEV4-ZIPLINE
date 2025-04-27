package com.zipline.repository.publicitem;

import com.zipline.entity.publicitem.PropertyArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyArticleViewRepository extends JpaRepository<PropertyArticle, Long>, JpaSpecificationExecutor<PropertyArticle> {
    /**
     * 지역 코드로 매물 페이지 조회
     */
    Page<PropertyArticle> findByRegionCode(String regionCode, Pageable pageable);
}
