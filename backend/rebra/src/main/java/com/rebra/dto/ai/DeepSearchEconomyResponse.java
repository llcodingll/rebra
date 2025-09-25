package com.rebra.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class DeepSearchEconomyResponse {
    private Detail detail;
    private int total_items;
    private int total_pages;
    private int page;
    private int page_size;
    private List<Article> data;

    @Data
    public static class Detail {
        private String message;
        private String code;
        private boolean ok;
    }

    @Data
    public static class Article {
        private String id;
        private List<String> sections;
        private String title;
        private String publisher;
        private String author;
        private String summary;
        private String image_url;
        private String thumbnail_url;
        private String content_url;
        private String published_at;
    }
}