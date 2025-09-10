package com.rebra.service;

import com.rebra.dto.NaverNewsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverNewsService {

    private final WebClient webClient;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.api-url:https://openapi.naver.com/v1/search/news.json}")
    private String newsUrl;

    public NaverNewsDto searchNews(String query, int display, int start, String sort) {
        log.info("네이버 뉴스 검색 시작: query={}, display={}, start={}, sort={}", query, display, start, sort);

        try {
            NaverNewsDto response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(newsUrl)
                            .queryParam("query", query)
                            .queryParam("display", display)
                            .queryParam("start", start)
                            .queryParam("sort", sort)
                            .build())
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .retrieve()
                    .bodyToMono(NaverNewsDto.class)
                    .block();

            log.info("네이버 뉴스 검색 완료: 총 {}건, 반환 {}건", response.getTotal(), response.getItems().size());
            return response;

        } catch (Exception e) {
            log.error("네이버 뉴스 검색 중 오류 발생: query={}, error={}", query, e.getMessage(), e);
            throw new RuntimeException("네이버 뉴스 검색 실패: " + e.getMessage(), e);
        }

    }

}