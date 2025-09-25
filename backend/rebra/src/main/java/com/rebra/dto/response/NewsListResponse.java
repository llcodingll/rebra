package com.rebra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "뉴스 목록 응답 DTO")
public class NewsListResponse {

    @Schema(description = "뉴스 목록")
    private List<NewsResponse> news;

    @Schema(description = "총 뉴스 개수", example = "5")
    private int totalCount;

    public static NewsListResponse from(List<NewsResponse> news) {
        return NewsListResponse.builder()
                .news(news)
                .totalCount(news.size())
                .build();
    }
}