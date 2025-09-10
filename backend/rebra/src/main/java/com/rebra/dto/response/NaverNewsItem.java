package com.rebra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverNewsItem {
    private String title;
    private String originalLink;
    private String link;
    private String description;
    private String pubDate;
}