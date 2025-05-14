package com.zipline.infrastructure.crawl;

import com.zipline.domain.entity.crawl.Crawl;
import com.zipline.domain.entity.enums.CrawlStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface CrawlRepository extends JpaRepository<Crawl, Long> {
    Crawl findByCortarNo(Long cortarNo);

    @Transactional
    @Modifying
    @Query("UPDATE Crawl r SET r.naverStatus = :status WHERE r.cortarNo = :cortarNo")
    void updateNaverCrawlStatus(@Param("cortarNo") Long cortarNo,
                                @Param("status") CrawlStatus status);

    @Transactional
    @Modifying
    @Query("UPDATE Crawl r SET r.naverStatus = :status, r.naverLastCrawledAt = :lastCrawledAt WHERE r.cortarNo = :cortarNo")
    void updateNaverCrawlStatusAndLastCrawledAt(@Param("cortarNo") Long cortarNo,
                                                @Param("status") CrawlStatus status,
                                                @Param("lastCrawledAt") LocalDateTime lastCrawledAt);

    @Query("SELECT r.cortarNo FROM Crawl r WHERE r.naverLastCrawledAt IS NULL OR r.naverLastCrawledAt < :cutoffDate OR r.naverStatus IN ('FAILED', 'PROCESSING')")
    Page<Long> findRegionsNeedingCrawlingUpdateForNaverWithPage(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Crawl m SET m.errorLog = :errorLog WHERE m.cortarNo = :cortarNo")
    void updateErrorLog(@Param("cortarNo") Long cortarNo, @Param("errorLog") String errorLog);


    @Query("SELECT c FROM Crawl c WHERE CAST(c.cortarNo AS string) LIKE CONCAT(:prefix, '%')")
    List<Crawl> findAllByRegionPrefix(@Param("prefix") String prefix);

    @Query("SELECT c FROM Crawl c WHERE c.cortarNo >= :lowerBound AND c.cortarNo < :upperBound")
    List<Crawl> findAllByRegionRange(
            @Param("lowerBound") Long lowerBound,
            @Param("upperBound") Long upperBound);

    @Query("SELECT c.cortarNo FROM Crawl c WHERE " +
            "CAST(c.cortarNo AS string) LIKE CONCAT(:prefix, '%') AND " +
            "(c.naverLastCrawledAt IS NULL OR c.naverLastCrawledAt < :cutoffDate) AND " +
            "c.naverCrawlStatus <> 'PROCESSING'")
    Page<Long> findRegionsWithPrefixNeedingCrawlingUpdateForNaver(
            @Param("prefix") String prefix,
            @Param("cutoffDate") LocalDateTime cutoffDate,
            Pageable pageable);
}