package com.zipline.infrastructure.naver;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.zipline.domain.entity.enums.MigrationStatus;
import com.zipline.domain.entity.naver.NaverRawArticle;

/**
 * 네이버 부동산 원본 데이터 저장소 인터페이스
 */
@Repository
public interface NaverRawArticleRepository extends JpaRepository<NaverRawArticle, Long> {
    /**
     * 매물 ID로 원본 데이터 조회
     */
    Optional<NaverRawArticle> findByArticleId(String articleId);

    /**
     * 지역 코드로 원본 데이터 목록 조회
     */
    List<NaverRawArticle> findByCortarNo(Long cortarNo);

    /**
     * 마이그레이션 상태별 원본 데이터 페이지 조회
     */
    Page<NaverRawArticle> findByMigrationStatus(MigrationStatus status, Pageable pageable);

    /**
     * 지역 코드와 마이그레이션 상태로 원본 데이터 페이지 조회
     */
    Page<NaverRawArticle> findByCortarNoAndMigrationStatus(Long cortarNo, MigrationStatus status, Pageable pageable);


    /**
     * 마이그레이션 상태 일괄 업데이트
     */
    @Modifying
    @Transactional
    @Query("UPDATE NaverRawArticle SET migrationStatus = :status WHERE migrationStatus = :currentStatus")
    int updateMigrationStatus(MigrationStatus currentStatus, MigrationStatus status);

    /**
     * 특정 지역 코드에 대한 마이그레이션 상태 초기화
     */
    @Modifying
    @Transactional
    @Query("UPDATE NaverRawArticle n SET n.migrationStatus = :status WHERE n.cortarNo = :cortarNo")
    int resetMigrationStatusForRegion(@Param("cortarNo") Long cortarNo, @Param("status") MigrationStatus status);

    long countByCortarNoAndMigrationStatus(Long cortarNo, MigrationStatus status);

    /**
     * 특정 기간 이후에 수집된 데이터 조회
     */
    List<NaverRawArticle> findByCreatedAtAfter(LocalDateTime date);

    long countByMigrationStatus(MigrationStatus status);

    @Procedure(name = "MigrateAllPendingArticles")
    void migrateAllPendingArticles();

    @Procedure(name = "MigrateArticlesByRegion")
    void migrateArticlesByRegion(@Param("region_code") Long regionCode);

    @Procedure(name = "RetryFailedMigrations")
    void retryFailedMigrations();

}
