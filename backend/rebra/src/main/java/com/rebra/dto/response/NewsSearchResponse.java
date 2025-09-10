package com.rebra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSearchResponse {
    private String overallSummary;
    private List<NewsItemResponse> newsItems;
    private int totalCount;
}