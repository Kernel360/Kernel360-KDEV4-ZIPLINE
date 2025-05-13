package com.zipline.service.zigbang.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipline.domain.entity.enums.PropertyCategory;
import com.zipline.infrastructure.crawl.fetch.Fetcher;
import com.zipline.infrastructure.zigbang.ZigBangArticleRepository;
import com.zipline.infrastructure.zigbang.ZigBangCrawlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service("defaultZigBangArticleCrawler")
public class DefaultZigBangArticleCrawler extends SharedZigbangCrawler {

    protected DefaultZigBangArticleCrawler(ObjectMapper objectMapper, ZigBangCrawlRepository crawlRepo, ZigBangArticleRepository articleRepo) {
        super(objectMapper, crawlRepo, articleRepo);
    }
    @Override
    public void executeCrawl(Fetcher fetcher) {
        log.info("=== 직방 전체 매물 정보 수집 시작 ===");

        Set<String> geohashes = getGeohashList();

        for (PropertyCategory category : PropertyCategory.supportedCategories()) {
            for (String geohash : geohashes) {
                try {
                    List<Long> itemIds = fetchItemIds(fetcher, geohash, category);
                    if (!itemIds.isEmpty()) {
                        fetchAndSaveItemDetails(fetcher, geohash, category, itemIds);
                        updateCrawlComplete(geohash, category);
                    }
                } catch (Exception e) {
                    String errorMsg = String.format("지오해시 %s, 카테고리 %s 처리 실패: %s", geohash, category, e.getMessage());
                    log.error(errorMsg, e);
                    updateCrawlError(geohash, category, errorMsg);
                }
            }
        }
        log.info("=== 직방 전체 매물 정보 수집 완료 ===");
    }
}