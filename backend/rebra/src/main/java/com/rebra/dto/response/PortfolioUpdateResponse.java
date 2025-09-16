package com.rebra.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PortfolioUpdateResponse {

    private Long portfolioId;
    private String name;
    private String description;
    private String message;
    private LocalDateTime updatedAt;

    public static PortfolioUpdateResponse of(Long portfolioId, String name, String description,
                                           LocalDateTime updatedAt) {
        return PortfolioUpdateResponse.builder()
            .portfolioId(portfolioId)
            .name(name)
            .description(description)
            .message("포트폴리오가 성공적으로 수정되었습니다.")
            .updatedAt(updatedAt)
            .build();
    }

    public static PortfolioUpdateResponse success(Long portfolioId, String message) {
        return PortfolioUpdateResponse.builder()
            .portfolioId(portfolioId)
            .message(message)
            .updatedAt(LocalDateTime.now())
            .build();
    }
}