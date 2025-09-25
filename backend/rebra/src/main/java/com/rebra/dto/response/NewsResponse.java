package com.rebra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "뉴스 응답 DTO")
public class NewsResponse {

    @Schema(description = "뉴스 ID", example = "1")
    private Long id;

    @Schema(description = "뉴스 제목", example = "코스피, 외국인 매수세에 상승 마감")
    private String title;

    @Schema(description = "AI 요약된 뉴스 내용", example = "코스피가 외국인 매수세에 힘입어 상승 마감했습니다. 반도체와 2차전지 관련주가 강세를 보였으며, 시장 전반적으로 긍정적인 분위기가 조성되었습니다.")
    private String summary;

    @Schema(description = "뉴스 원본 URL", example = "https://news.example.com/economy/12345")
    private String url;

    @Schema(description = "뉴스 이미지 URL", example = "https://image.example.com/news/12345.jpg")
    private String imageUrl;

    @Schema(description = "뉴스 발행일시", example = "2024-01-24T09:30:00")
    private LocalDateTime publishedAt;
}