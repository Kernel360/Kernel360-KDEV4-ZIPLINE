package com.zipline.infrastructure.crawl.fetch.dto;

import com.zipline.infrastructure.proxy.dto.ProxyInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class FetchConfigDTO {
    private String requestMethod;
    private boolean doOutput;
    private String accept;
    private String contentType;
    private String host;
    private String referer;
    private String secChUa;
    private String secChUaMobile;
    private String secChUaPlatform;
    private String secFetchDest;
    private String secFetchMode;
    private String secFetchSite;
    private String userAgent;
    private String acceptLanguage;
    private int connectTimeout;
    private int readTimeout;
    @Builder.Default
    private Map<String, String> extraHeader = new HashMap<>();

    public static FetchConfigDTO regionDefaultConfig() {
        return FetchConfigDTO.builder()
                .requestMethod("GET")
                .doOutput(false)
                .accept("application/json")
                .host("new.land.naver.com")
                .referer("https://new.land.naver.com/")
                .secChUa("\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"")
                .secChUaMobile("?0")
                .secChUaPlatform("\"Windows\"")
                .secFetchDest("empty")
                .secFetchMode("cors")
                .secFetchSite("same-origin")
                .userAgent("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36")
                .acceptLanguage("ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .connectTimeout(5000)
                .readTimeout(10000)
                .build();
    }

    public static FetchConfigDTO naverDefaultConfig() {
        return FetchConfigDTO.builder()
                .requestMethod("GET")
                .doOutput(false)
                .accept("application/json")
                .host("m.land.naver.com")
                .referer("https://m.land.naver.com/")
                .secChUa("\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"")
                .secChUaMobile("?1")
                .secChUaPlatform("\"Android\"")
                .secFetchDest("empty")
                .secFetchMode("cors")
                .secFetchSite("same-origin")
                .userAgent("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36")
                .acceptLanguage("ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .connectTimeout(5000)
                .readTimeout(10000)
                .build();
    }

    public static FetchConfigDTO zigbangDefaultConfig() {
        return FetchConfigDTO.builder()
                .requestMethod("GET")
                .doOutput(false)
                .accept("application/json")
                .contentType("application/json; charset=UTF-8")
                .host("https://www.zigbang.com/")
                .referer("https://www.zigbang.com/")
                .secChUa("\"Not A(Brand\";v=\"99\", \"Chromium\";v=\"122\", \"Google Chrome\";v=\"122\"")
                .secChUaMobile("?0")
                .secChUaPlatform("\"Windows\"")
                .secFetchDest("empty")
                .secFetchMode("cors")
                .secFetchSite("cross-site")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .acceptLanguage("ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .connectTimeout(10000)
                .readTimeout(15000)
                .build();
    }

    public static FetchConfigDTO zigbangPostConfig() {
        FetchConfigDTO config = FetchConfigDTO.builder()
                .requestMethod("POST")
                .doOutput(true)
                .accept("application/json")
                .host("apis.zigbang.com")
                .referer("https://www.zigbang.com/")
                .secChUa("\"Not A(Brand\";v=\"99\", \"Chromium\";v=\"122\", \"Google Chrome\";v=\"122\"")
                .secChUaMobile("?0")
                .secChUaPlatform("\"Windows\"")
                .secFetchDest("empty")
                .secFetchMode("cors")
                .secFetchSite("same-site")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .acceptLanguage("ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .connectTimeout(10000)
                .readTimeout(15000)
                .build();
        config.getExtraHeader().put("X-Zigbang-Platform", "www");
        return config;
    }

    public FetchConfigDTO addHeader(String key, String value) {
        if (extraHeader == null) {
            extraHeader = new HashMap<>();
        }
        extraHeader.put(key, value);
        return this;
    }
}
