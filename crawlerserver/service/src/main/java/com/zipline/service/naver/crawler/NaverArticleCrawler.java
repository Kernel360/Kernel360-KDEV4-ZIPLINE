package com.zipline.service.naver.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipline.domain.entity.crawl.Crawl;
import com.zipline.domain.entity.enums.CrawlStatus;
import com.zipline.domain.entity.enums.MigrationStatus;
import com.zipline.domain.entity.enums.Platform;
import com.zipline.domain.entity.naver.NaverRawArticle;
import com.zipline.domain.entity.region.Region;
import com.zipline.global.util.CoordinateUtil;
import com.zipline.global.util.RandomSleepUtil;
import com.zipline.infrastructure.crawl.CrawlRepository;
import com.zipline.infrastructure.crawl.fetch.Fetcher;
import com.zipline.infrastructure.crawl.fetch.dto.FetchConfigDTO;
import com.zipline.infrastructure.naver.NaverRawArticleRepository;
import com.zipline.infrastructure.region.RegionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service("naverArticleCrawler")
public class NaverArticleCrawler {

    private final ObjectMapper objectMapper;
    private final RegionRepository regionRepo;
    private final NaverRawArticleRepository articleRepo;
    private final CrawlRepository crawlRepo;

    @Value("${crawler.recent-days:14}")
    private int recentDays;

    @Value("${crawler.page-size:100}")
    private int pageSize;

    @Value("${crawler.max-retry-count:10}")
    private int maxRetryCount;

    public NaverArticleCrawler(
            ObjectMapper objectMapper,
            CrawlRepository crawlRepo,
            RegionRepository regionRepo,
            NaverRawArticleRepository articleRepo
    ) {
        this.objectMapper = objectMapper;
        this.crawlRepo = crawlRepo;
        this.regionRepo = regionRepo;
        this.articleRepo = articleRepo;
    }

    public void executeCrawl(Fetcher fetcher) {
        log.info("=== 네이버 원본 매물 정보 수집 시작 ===");
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(recentDays);

        int pageNumber = 0;
        boolean hasMore = true;

        while (hasMore) {
            Page<Long> regions = crawlRepo.findRegionsNeedingCrawlingUpdateForNaverWithPage(cutoffDate,
                    PageRequest.of(pageNumber, pageSize));
            if (regions.isEmpty()) break;

            regions.getContent().forEach(region -> {
                executeCrawlForRegion(fetcher,region);
            });

            pageNumber++;
            hasMore = pageNumber < regions.getTotalPages();
        }

        log.info("=== 네이버 원본 매물 정보 수집 완료 ===");
    }

    @Transactional
    public void executeCrawlForRegion(Fetcher fetcher, Long cortarNo) {
        log.info("네이버 원본 매물 수집 시작 - 지역 코드: {}", cortarNo);
        crawlRepo.updateNaverCrawlStatus(cortarNo, CrawlStatus.PROCESSING);

        ObjectMapper objectMapper = new ObjectMapper();
        int page = 1;
        boolean hasMore = true;
        boolean is307 = false;
        int total = 0;

        try {
            Crawl crawlRegion = crawlRepo.findByCortarNo(cortarNo);
            while (hasMore) {
                String apiUrl = buildApiUrl(crawlRegion, page++);
                String response = fetcher.fetch(apiUrl, FetchConfigDTO.naverDefaultConfig());

                if (response == "307") {
                    String errorMessage = String.format("[ERROR307]"+"네이버 원본 데이터 조회 실패 307: API URL: %s", apiUrl);
                    log.warn(errorMessage);
                    crawlRegion.errorWithLog(Platform.NAVER, errorMessage, 1000, CrawlStatus.ERROR307);
                    crawlRepo.save(crawlRegion);
                    is307 = true;
                    break;
                }

                if (!response.isEmpty()) {
                    JsonNode result = objectMapper.readTree(response);
                    JsonNode articles = result.path("body");

                    if (articles.isArray()) {
                        for (JsonNode node : articles) {
                            saveRawArticle(node, cortarNo);
                            total++;
                        }
                    }
                    hasMore = result.path("more").asBoolean();
                    RandomSleepUtil.sleep();
                }
            }
            LocalDateTime now = LocalDateTime.now();
            if (!is307) crawlRepo.updateNaverCrawlStatusAndLastCrawledAt(cortarNo, CrawlStatus.COMPLETED, now);
            log.info("완료 - 지역: {}, 총 매물 수: {}", crawlRegion.getCortarNo(), total);
        } catch (Exception e) {
            String errorMessage = String.format("[FAILED]"+"지역 %s 처리 실패: %s", cortarNo, e.getMessage());
            log.error(errorMessage, e);
            Crawl crawl = crawlRepo.findByCortarNo(cortarNo);
            crawl.errorWithLog(Platform.NAVER, errorMessage, 1000, CrawlStatus.FAILED);
            crawlRepo.save(crawl);
        }
    }

    private String buildApiUrl(Crawl crawlRegion, int page) {
        Optional<Region> region = regionRepo.findByCortarNo(crawlRegion.getCortarNo());
        Double lat = region.map(Region::getCenterLat).orElse(null);
        Double lon = region.map(Region::getCenterLon).orElse(null);

        if (lat == null || lon == null) {
            log.error("지역 좌표 정보 없음: {}", crawlRegion.getCortarNo());
            throw new RuntimeException("지역 좌표 정보 없음: " + crawlRegion.getCortarNo());
        }

        double[] bounds = CoordinateUtil.calculateBounds(
                lat,
                lon,
                12
        );
        return String.format(
                "https://m.land.naver.com/cluster/ajax/articleList?" +
                        "rletTpCd=APT:OPST:VL:YR:DSD:ABYG:OBYG:JGC:JWJT:DDDGG:SGJT:HOJT:JGB:OR:GSW:SG:SMS:GJCG:GM:TJ:APTHGJ&" +
                        "tradTpCd=A1:B1:B2:B3&z=12&lat=%.6f&lon=%.6f&btm=%.6f&lft=%.6f&top=%.6f&rgt=%.6f&cortarNo=%d&sort=rank&page=%d",
                lat,
                lon,
                bounds[2],
                bounds[3],
                bounds[0],
                bounds[1],
                region.get().getCortarNo(),
                page
        );
    }

    private void saveRawArticle(JsonNode node, Long cortarNo) {
        String articleId = node.path("atclNo").asText();
        Optional<NaverRawArticle> existing = articleRepo.findByArticleId(articleId);

        NaverRawArticle raw = existing.map(e -> NaverRawArticle.builder()
                        .id(e.getId())
                        .articleId(articleId)
                        .cortarNo(cortarNo)
                        .rawData(node.toString())
                        .migrationStatus(MigrationStatus.PENDING)
                        .createdAt(e.getCreatedAt())
                        .build())
                .orElseGet(() -> NaverRawArticle.builder()
                        .articleId(articleId)
                        .cortarNo(cortarNo)
                        .rawData(node.toString())
                        .migrationStatus(MigrationStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build());

        articleRepo.save(raw);
    }

    public void crawlRegionsByPrefix(Fetcher fetcher, Long cortarNo) {
        String prefix = String.valueOf(cortarNo).substring(0, 2);
        log.info("지역 접두사로 크롤링 시작: {}", prefix);
        executeCrawlForRegionPrefix(fetcher, prefix);
    }
    public void executeCrawlForRegionPrefix(Fetcher fetcher, String regionPrefix) {
        log.info("=== 네이버 원본 매물 정보 수집 시작 (지역 접두사: {}) ===", regionPrefix);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(recentDays);

        int pageNumber = 0;
        boolean hasMore = true;
        int totalRegions = 0;
        int successRegions = 0;
        int failedRegions = 0;

        try {
            while (hasMore) {
                Page<Long> regions = crawlRepo.findRegionsWithPrefixNeedingCrawlingUpdateForNaver(
                        regionPrefix,
                        cutoffDate,
                        PageRequest.of(pageNumber, pageSize));

                if (regions.isEmpty()) break;

                totalRegions += regions.getNumberOfElements();
                log.info("지역 접두사 {}에 대한 처리 - 페이지 {}, 지역 개수: {}",
                        regionPrefix, pageNumber + 1, regions.getNumberOfElements());

                for (Long region : regions.getContent()) {
                    try {
                        executeCrawlForRegion(fetcher, region);
                        successRegions++;
                        RandomSleepUtil.sleep();
                    } catch (Exception e) {
                        failedRegions++;
                        log.error("지역 {} 크롤링 실패: {}", region, e.getMessage());
                    }
                }

                pageNumber++;
                hasMore = pageNumber < regions.getTotalPages();
            }

            log.info("=== 네이버 원본 매물 정보 수집 완료 (지역 접두사: {}) ===", regionPrefix);
            log.info("총 처리 지역: {}, 성공: {}, 실패: {}", totalRegions, successRegions, failedRegions);
        } catch (Exception e) {
            log.error("지역 접두사 {} 크롤링 중 오류 발생: {}", regionPrefix, e.getMessage(), e);
            throw new RuntimeException("Region prefix crawling failed", e);
        }
    }

    private String extractRegionPrefix(Long cortarNo) {
        String cortarNoStr = String.valueOf(cortarNo);
        if (cortarNoStr.length() < 2) {
            throw new IllegalArgumentException("Invalid cortarNo format: " + cortarNo);
        }
        return cortarNoStr.substring(0, 2);
    }


    public void executeCrawlForAllRelatedRegions(Fetcher fetcher, Long cortarNo) {
        String prefix = extractRegionPrefix(cortarNo);
        executeCrawlForRegionPrefix(fetcher, prefix);
    }

    private Long[] calculateRegionBounds(String prefix) {
        if (prefix.length() != 2 || !prefix.matches("\\d{2}")) {
            throw new IllegalArgumentException("Region prefix must be exactly 2 digits");
        }

        long lowerBound = Long.parseLong(prefix + "00000000");
        long upperBound = (Long.parseLong(prefix) + 1) + 00000000L;

        return new Long[] {lowerBound, upperBound};
    }

    public void executeCrawlForRegionPrefixUsingRange(Fetcher fetcher, String prefix) {
        log.info("=== 네이버 원본 매물 정보 수집 시작 (지역 접두사 범위: {}) ===", prefix);

        Long[] bounds = calculateRegionBounds(prefix);
        Long lowerBound = bounds[0];
        Long upperBound = bounds[1];

        List<Crawl> regions = crawlRepo.findAllByRegionRange(lowerBound, upperBound);

        log.info("지역 접두사 {}에 해당하는 지역 {}개 발견", prefix, regions.size());

        int successCount = 0;
        int failCount = 0;

        for (Crawl region : regions) {
            try {
                executeCrawlForRegion(fetcher, region.getCortarNo());
                successCount++;
                RandomSleepUtil.sleep();
            } catch (Exception e) {
                failCount++;
                log.error("지역 {} 크롤링 실패: {}", region.getCortarNo(), e.getMessage());
            }
        }

        log.info("=== 네이버 원본 매물 정보 수집 완료 (지역 접두사: {}) ===", prefix);
        log.info("총 처리 지역: {}, 성공: {}, 실패: {}", regions.size(), successCount, failCount);
    }

}