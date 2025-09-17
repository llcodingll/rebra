package com.rebra.dummy.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UpdateCreatedAtResponse {

    private String message;
    private Long portfolioId;
    private LocalDateTime updatedCreatedAt;
    private LocalDateTime updatedAt;

    public static UpdateCreatedAtResponse success(Long portfolioId, LocalDateTime updatedCreatedAt) {
        return UpdateCreatedAtResponse.builder()
                .message("포트폴리오 생성일시 업데이트 완료")
                .portfolioId(portfolioId)
                .updatedCreatedAt(updatedCreatedAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}