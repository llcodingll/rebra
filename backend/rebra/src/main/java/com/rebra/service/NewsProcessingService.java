package com.rebra.service;

import com.rebra.dto.NaverNewsDto;
import com.rebra.dto.response.NewsItemResponse;
import com.rebra.dto.response.NewsSearchResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsProcessingService {

    private final NaverNewsService naverNewsService;
    private final GMSService gmsService;

    public NewsSearchResponse processStockNews(String stockKeyword, int display, int start, String sort) {
        log.info("주식 뉴스 처리 시작: keyword={}, display={}, start={}, sort={}", stockKeyword, display, start, sort);

        try {
            // 1. 네이버 뉴스 검색
            NaverNewsDto naverNews = naverNewsService.searchNews(stockKeyword, display, start, sort);

            // 2. GMS로 뉴스 요약
            String summary = gmsService.summarizeNews(naverNews);

            // 3. 개별 뉴스 아이템을 NewsItemResponse로 변환
            List<NewsItemResponse> newsItems = IntStream.range(0, naverNews.getItems().size())
                    .mapToObj(i -> {
                        var item = naverNews.getItems().get(i);
                        return NewsItemResponse.builder()
                                .id((long) (start + i))
                                .title(cleanHtmlTags(item.getTitle()))
                                .summary(cleanHtmlTags(item.getDescription()))
                                .originalLink(item.getOriginalLink())
                                .link(item.getLink())
                                .publishedAt(parsePublishDate(item.getPubDate()))
                                .build();
                    })
                    .toList();

            log.info("주식 뉴스 처리 완료: {}건 처리됨", newsItems.size());
            return NewsSearchResponse.builder()
                    .overallSummary(summary)
                    .newsItems(newsItems)
                    .totalCount(naverNews.getTotal())
                    .build();

        } catch (Exception e) {
            log.error("주식 뉴스 처리 중 오류 발생: keyword={}, error={}", stockKeyword, e.getMessage(), e);
            throw new RuntimeException("주식 뉴스 처리 실패: " + e.getMessage(), e);
        }
    }

    private String cleanHtmlTags(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("<[^>]*>", "").replaceAll("&quot;", "\"").replaceAll("&amp;", "&");
    }

    private LocalDateTime parsePublishDate(String pubDate) {
        try {
            // 네이버 API 날짜 형식: "Wed, 04 Dec 2024 10:30:00 +0900"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");
            return LocalDateTime.parse(pubDate, formatter);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", pubDate);
            return LocalDateTime.now();
        }
    }
}