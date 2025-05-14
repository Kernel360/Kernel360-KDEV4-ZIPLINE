package com.zipline.service.region.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipline.domain.entity.region.Region;
import com.zipline.global.util.RandomSleepUtil;
import com.zipline.infrastructure.crawl.fetch.Fetcher;
import com.zipline.infrastructure.crawl.fetch.dto.FetchConfigDTO;
import com.zipline.infrastructure.region.RegionRepository;
import com.zipline.service.region.dto.RegionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service("RegionCrawler")
public class RegionCrawler {

    private final RegionRepository regionRepository;
    private static final String API_BASE_URL = "https://new.land.naver.com/api/regions/list?cortarNo=";
    private static final Long KOREA_CORTAR_NO = 0L;

    public void executeCrawl(Fetcher fetcher) {
        log.info("지역 정보 수집 시작");
        initializeKoreaIfNotExists();
        executeLevel(1, fetcher);
        executeLevel(2, fetcher);
        executeLevel(3, fetcher);
        logCollectionSummary();
    }

    @Transactional
    private void initializeKoreaIfNotExists() {
        if (regionRepository.findByCortarNo(KOREA_CORTAR_NO).isEmpty()) {
            Region koreaRegion = RegionDTO.createKoreaRegion();
            regionRepository.save(koreaRegion);
            log.info("한국 지역 생성 성공");
        }
    }

    private void executeLevel(int level, Fetcher fetcher) {
        List<Region> parents = regionRepository.findByLevel(level - 1);
        int total = parents.size();
        int count = 0;

        for (Region parent : parents) {
            collectRegionsForParent(fetcher, parent.getCortarNo(), level);
            count++;
            if (count % 10 == 0 || count == total) {
                log.info("수집 중: 레벨 {} - {} / {} 완료", level, count, total);
            }
            RandomSleepUtil.sleepShort();
        }
    }

    private void collectRegionsForParent(Fetcher fetcher, Long parentCortarNo, int targetLevel) {
        String url = API_BASE_URL + String.format("%010d", parentCortarNo);

        FetchConfigDTO fetchConfig = FetchConfigDTO.regionDefaultConfig();
        try {
            String response = fetcher.fetch(url,fetchConfig);
            if (response == null) {
                log.error("프록시 정보 수집 중 오류: 307에러");
                return;
            }
            if (!response.isEmpty()) {
                List<RegionDTO> regions = parseRegions(response);
                regions.forEach(dto -> saveRegion(parentCortarNo, dto, targetLevel));
            }
        } catch (Exception e) {
            log.error("프록시 정보 수집 중 오류: {}", e.getMessage());
        }
    }

    private void saveRegion(Long parentCortarNo, RegionDTO dto, int level) {
        try {
            Region parentRegion = regionRepository.findByCortarNo(parentCortarNo)
                    .orElseThrow(() -> new IllegalArgumentException("부모 지역 미발견: " + parentCortarNo));
            Region region = dto.toEntity(level, parentRegion);
            regionRepository.save(region);
        } catch (Exception e) {
            log.error("지역 저장 중 오류: {}", dto.getCortarName(), e.getMessage());
        }
    }

    private List<RegionDTO> parseRegions(String jsonResponse) {
        List<RegionDTO> regions = new ArrayList<>();
        try {
            JsonNode regionsNode = new ObjectMapper()
                    .readTree(jsonResponse)
                    .path("regionList");

            if (regionsNode.isArray()) {
                for (JsonNode regionNode : regionsNode) {
                    regions.add(RegionDTO.fromJsonNode(regionNode));
                }
            }
        } catch (Exception e) {
            log.error("지역 정보 파싱 중 오류 발생: {}", e.getMessage());
        }
        return regions;
    }

    private void logCollectionSummary() {
        List<Region> allRegions = regionRepository.findAll();
        log.info("수집 완료 - 전체: {}, 시/도: {}, 시/군/구: {}, 읍/면/동: {}",
                allRegions.size(),
                regionRepository.findByLevel(1).size(),
                regionRepository.findByLevel(2).size(),
                regionRepository.findByLevel(3).size()
        );
    }

    public void executeCrawlForRegion(Fetcher fetcher, Long cortarNo) {
        int targetLevel = regionRepository.findLevelByCortarNo(cortarNo);
        collectRegionsForParent(fetcher, cortarNo, targetLevel);
        log.info("특정 지역 ({})에 대한 하위 지역 수집 완료", cortarNo);
    }
}
