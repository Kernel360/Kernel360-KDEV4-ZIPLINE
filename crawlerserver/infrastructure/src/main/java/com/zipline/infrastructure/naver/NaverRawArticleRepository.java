package com.zipline.infrastructure.naver;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    Optional<NaverRawArticle> findByArticleId(String articleId);

    Page<NaverRawArticle> findByMigrationStatus(MigrationStatus status, Pageable pageable);

    Page<NaverRawArticle> findByCortarNoAndMigrationStatus(Long cortarNo, MigrationStatus status, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE NaverRawArticle SET migrationStatus = :status WHERE migrationStatus = :currentStatus")
    int updateMigrationStatus(MigrationStatus currentStatus, MigrationStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE NaverRawArticle n SET n.migrationStatus = :status WHERE n.cortarNo = :cortarNo")
    int resetMigrationStatusForRegion(@Param("cortarNo") Long cortarNo, @Param("status") MigrationStatus status);

    long countByCortarNoAndMigrationStatus(Long cortarNo, MigrationStatus status);

    List<NaverRawArticle> findByCreatedAtAfter(LocalDateTime date);

    long countByMigrationStatus(MigrationStatus status);

    List<NaverRawArticle> findByCortarNo(Long cortarNo);

    // Find all articles with cortarNo matching a specific prefix pattern
    @Query("SELECT n FROM NaverRawArticle n WHERE CAST(n.cortarNo AS string) LIKE CONCAT(:prefix, '%')")
    List<NaverRawArticle> findAllByRegionPrefix(@Param("prefix") String prefix);

    // Find all articles within a cortarNo range (better performance for numeric comparison)
    @Query("SELECT n FROM NaverRawArticle n WHERE n.cortarNo >= :lowerBound AND n.cortarNo < :upperBound")
    List<NaverRawArticle> findAllByRegionRange(
            @Param("lowerBound") Long lowerBound,
            @Param("upperBound") Long upperBound);

    // Count articles by region prefix
    @Query("SELECT COUNT(n) FROM NaverRawArticle n WHERE CAST(n.cortarNo AS string) LIKE CONCAT(:prefix, '%')")
    Long countByRegionPrefix(@Param("prefix") String prefix);
}
