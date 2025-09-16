package com.rebra.dto.request;

import lombok.Data;

/**
 * 포트폴리오 생성 요청 DTO
 */
@Data
public class PortfolioCreateRequest {

    private String name;        // 포트폴리오 이름
    private String description; // 포트폴리오 설명
    private Long accountId;     // 연결할 계좌 ID
}