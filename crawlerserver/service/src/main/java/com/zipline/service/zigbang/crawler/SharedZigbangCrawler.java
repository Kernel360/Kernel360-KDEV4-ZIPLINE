package com.zipline.service.zigbang.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipline.domain.entity.enums.CrawlStatus;
import com.zipline.domain.entity.enums.PropertyCategory;
import com.zipline.domain.entity.zigbang.ZigBangArticle;
import com.zipline.domain.entity.zigbang.ZigBangCrawl;
import com.zipline.global.util.GeoHashGenerator;
import com.zipline.global.util.RandomSleepUtil;
import com.zipline.global.util.UrlEncodingUtil;
import com.zipline.infrastructure.crawl.fetch.Fetcher;
import com.zipline.infrastructure.crawl.fetch.dto.FetchConfigDTO;
import com.zipline.infrastructure.zigbang.ZigBangArticleRepository;
import com.zipline.infrastructure.zigbang.ZigBangCrawlRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class SharedZigbangCrawler implements ZigBangArticleCrawler {

    private static final String ITEM_DETAIL_POST_URL = "https://apis.zigbang.com/v2/items/list ";

    private UrlEncodingUtil utf8 = new UrlEncodingUtil();

    private static final int LIMIT = 20;
    private static final int GEOHASH_PRECISION = 5;

    // 서울 위도/경도 범위
    private static final double MIN_LAT = 37.4300;
    private static final double MAX_LAT = 37.6800;
    private static final double MIN_LON = 126.7900;
    private static final double MAX_LON = 127.1900;

    // 격자 간격
    private static final double LAT_STEP = 0.05;
    private static final double LON_STEP = 0.05;

    // 한번에 요청할 아이템 개수
    private static final int ITEMS_PER_REQUEST = 14;

    protected final ObjectMapper objectMapper;
    protected final ZigBangCrawlRepository crawlRepo;
    protected final ZigBangArticleRepository articleRepo;

    public SharedZigbangCrawler(ObjectMapper objectMapper,
                                ZigBangCrawlRepository crawlRepo,
                                ZigBangArticleRepository articleRepo) {
        this.objectMapper = objectMapper;
        this.crawlRepo = crawlRepo;
        this.articleRepo = articleRepo;
    }

    // 공통 메서드
    protected Set<String> getGeohashList() {
        return GeoHashGenerator.generateGeoHashs(GEOHASH_PRECISION, MIN_LAT, MAX_LAT, MIN_LON, MAX_LON, LAT_STEP, LON_STEP);
    }


    protected List<Long> parseItemIdsFromResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get("items");

            List<Long> ids = new ArrayList<>();
            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode node : itemsNode) {
                    JsonNode idNode = node.get("itemId");
                    if (idNode != null && idNode.isNumber()) {
                        ids.add(idNode.asLong());
                    }
                }
            }

            return ids;
        } catch (Exception e) {
            log.warn("item_id 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    protected boolean checkHasMore(String response) {
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(response, Map.class);
            return Boolean.TRUE.equals(jsonMap.get("hasNext"));
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isAlreadyCompleted(String geohash, PropertyCategory category) {
        return crawlRepo.findById(ZigBangCrawl.buildId(geohash, category))
                .map(c -> c.getStatus() == CrawlStatus.COMPLETED)
                .orElse(false);
    }

    protected void updateCrawlComplete(String geohash, PropertyCategory category) {
        crawlRepo.findById(ZigBangCrawl.buildId(geohash, category))
                .map(c -> c.updateStatus(CrawlStatus.COMPLETED))
                .ifPresent(c -> crawlRepo.save(c));
    }

    protected void updateCrawlError(String geohash, PropertyCategory category, String message) {
        ZigBangCrawl crawl = crawlRepo.findById(ZigBangCrawl.buildId(geohash, category))
                .orElse(ZigBangCrawl.create(geohash, category));
        crawlRepo.save(crawl.errorWithLog(message, 1000, CrawlStatus.FAILED));
    }

    protected void  fetchAndSaveItemDetails(Fetcher fetcher, String geohash, PropertyCategory category, List<Long> itemIds) throws Exception {
        log.info("직방 매물 상세 정보 수집 시작 - geohash: {}, category: {}, 총 건수: {}", geohash, category, itemIds.size());

        FetchConfigDTO config = FetchConfigDTO.zigbangPostConfig();
        RandomSleepUtil randomSleepUtil =  new RandomSleepUtil();

        for (int i = 0; i < itemIds.size(); i += ITEMS_PER_REQUEST) {
            int end = Math.min(i + ITEMS_PER_REQUEST, itemIds.size());
            List<Long> subList = itemIds.subList(i, end);
            String body = buildJsonBody(subList);
            log.info("바디 확인" + body);
            String response = fetcher.fetchPost(ITEM_DETAIL_POST_URL, body, config);
            if (response != null && !response.isEmpty()) {
                saveRawArticles(geohash, category, response);
            }else{
                log.warn("지오해시 {} / 카테고리 {} 요청 실패 → 빈 리스트로 대체", geohash, category);
            }
            RandomSleepUtil.sleep();
        }
        log.info("직방 매물 상세 정보 수집 완료 - geohash: {}, category: {}", geohash, category);
    }

    private static String buildJsonBody(List<Long> itemIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"domain\": \"zigbang\", \"item_ids\": [");

        for (int i = 0; i < itemIds.size(); i++) {
            sb.append(itemIds.get(i));
            if (i < itemIds.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]}");
        return sb.toString();
    }


    protected List<Long> fetchItemIds(Fetcher fetcher, String geohash, PropertyCategory category) throws Exception {
        FetchConfigDTO config = FetchConfigDTO.zigbangDefaultConfig();
        int offset = 0;
        boolean hasMore = true;
        List<Long> itemIds = new ArrayList<>();

        log.info("직방 매물 ID 수집 시작 - geohash: {}, category: {}", geohash, category);
        ZigBangCrawl crawl = ZigBangCrawl.create(geohash, category).updateStatus(CrawlStatus.PROCESSING);
        crawlRepo.save(crawl);

        while (hasMore) {
            String listUrl = buildListUrl(category, geohash) + "&offset=" + offset + "&limit=" + LIMIT;
            String response = fetcher.fetch(listUrl, config);

            if (response != null && !response.isEmpty()) {
                List<Long> ids = parseItemIdsFromResponse(response);
                itemIds.addAll(ids);
                hasMore = checkHasMore(response);
                offset += LIMIT;
            } else {
                log.warn("지오해시 {} / 카테고리 {} 요청 실패 → 빈 리스트로 대체", geohash, category);
                return Collections.emptyList();
            }
            RandomSleepUtil.sleep();
        }

        log.info("직방 매물 ID 수집 완료 - geohash: {}, category: {}, 총 건수: {}", geohash, category, itemIds.size());
        return itemIds;
    }

    protected void saveRawArticles(String geohash, PropertyCategory category, String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get("items");

            if (itemsNode != null && itemsNode.isArray()) {
                List<String> idsToFetch = new ArrayList<>();
                Map<String, JsonNode> nodeMap = new HashMap<>();

                for (JsonNode node : itemsNode) {
                    String id = node.get("item_id").toString();
                    idsToFetch.add(id);
                    nodeMap.put(id, node);
                }

                List<ZigBangArticle> existingArticles = articleRepo.findAllByArticleIdIn(idsToFetch);
                Set<String> existingIds = existingArticles.stream()
                        .map(ZigBangArticle::getArticleId)
                        .collect(Collectors.toSet());

                List<ZigBangArticle> articlesToSave = new ArrayList<>();
                for (String id : idsToFetch) {
                    JsonNode node = nodeMap.get(id);
                    String dataString = node.toString();

                    ZigBangArticle article;

                    if (existingIds.contains(id)) {
                        article = existingArticles.stream()
                                .filter(a -> a.getId().equals(id))
                                .findFirst()
                                .orElseThrow();
                        article.update(geohash, category, dataString);
                    } else {
                        article = new ZigBangArticle();
                        article.create(id, geohash, category, dataString);
                    }
                    articlesToSave.add(article);
                }
                articleRepo.saveAll(articlesToSave);
            }
            log.info("직방 매물 상세데이터 저장 완료 - geohash: {}, item_count: {}", geohash, itemsNode.size());
        } catch (Exception e) {
            log.error("직방 매물 저장 실패 - geohash: {}, 오류: {}", geohash, e.getMessage());
        }
    }
    protected String buildListUrl(PropertyCategory category, String geohash) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://apis.zigbang.com/v2/items/");
        sb.append(utf8.encode(category.getApiPath()));
        sb.append("?geohash=");
        sb.append(utf8.encode(geohash));
        sb.append("&depositMin=0&rentMin=0");
        sb.append("&salesTypes[0]=").append(utf8.encode("전세"));
        sb.append("&salesTypes[1]=").append(utf8.encode("월세"));
        if (category.supportsSaleType()) {
            sb.append("&salesTypes[2]=").append(utf8.encode("매매"));
        }
        sb.append("&domain=zigbang&checkAnyItemWithoutFilter=true");
        return sb.toString();
    }
}
