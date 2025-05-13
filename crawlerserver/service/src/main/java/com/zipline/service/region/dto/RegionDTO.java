package com.zipline.service.region.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.zipline.domain.entity.region.Region;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class RegionDTO {
    private Long cortarNo;
    private String cortarName;
    private Double centerLat;
    private Double centerLon;
    private Integer level;

    public static RegionDTO from(Region region) {
        return RegionDTO.builder()
            .cortarNo(region.getCortarNo())
            .cortarName(region.getCortarName())
            .centerLat(region.getCenterLat())
            .centerLon(region.getCenterLon())
            .level(region.getLevel())
            .build();
    }

    /**
     * 지역 목록을 DTO 목록으로 변환합니다.
     */
    public static List<RegionDTO> fromList(List<Region> regions) {
        return regions.stream()
            .map(RegionDTO::from)
            .collect(Collectors.toList());
    }

    public static RegionDTO fromJsonNode(JsonNode node) {
        return RegionDTO.builder()
                .cortarNo(node.path("cortarNo").asLong())
                .cortarName(node.path("cortarName").asText())
                .centerLat(node.path("centerLat").asDouble())
                .centerLon(node.path("centerLon").asDouble())
                .build();
    }

    public Region toEntity(int level, Region parent) {
        return Region.builder()
                .cortarNo(this.getCortarNo())
                .cortarName(this.getCortarName())
                .level(level)
                .centerLat(this.getCenterLat())
                .centerLon(this.getCenterLon())
                .parent(parent)
                .build();
    }
}
