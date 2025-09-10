package com.rebra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsItemResponse {
    private Long id;
    private String title;
    private String summary;
    private String originalLink;
    private String link;
    private LocalDateTime publishedAt;
}