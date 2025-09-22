package com.rebra.client;

import com.rebra.config.FssApiProperties;
import com.rebra.dto.external.FssMarketIndexResponse;
import com.rebra.exception.external.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FssMarketIndexApiClient {

    private final RestTemplate restTemplate;
    private final FssApiProperties fssApiProperties;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final List<String> TARGET_INDICES = Arrays.asList("코스피", "코스피 200", "KRX 300", "코스닥");

    public List<FssMarketIndexResponse.MarketIndexItem> getLatestMarketIndices() {
        if (!StringUtils.hasText(fssApiProperties.getServiceKey())) {
            throw new ExternalApiException("FSS API 서비스 키가 설정되지 않았습니다");
        }

        // 최대 10일 전까지 거슬러 올라가며 데이터 찾기
        LocalDate currentDate = LocalDate.now();
        
        for (int daysBack = 1; daysBack <= 10; daysBack++) {
            LocalDate targetDate = currentDate.minusDays(daysBack);
            log.info("시장 지수 조회 시도 - 날짜: {}", targetDate);
            
            List<FssMarketIndexResponse.MarketIndexItem> allIndices = new ArrayList<>();
            boolean hasValidData = false;
            
            for (String indexName : TARGET_INDICES) {
                try {
                    log.debug("시장 지수 조회 시작 - 지수명: {}, 날짜: {}", indexName, targetDate);
                    
                    FssMarketIndexResponse.MarketIndexItem indexData = getMarketIndexByName(indexName, targetDate);
                    if (indexData != null) {
                        allIndices.add(indexData);
                        hasValidData = true;
                        log.info("시장 지수 조회 성공 - 지수명: {}, 날짜: {}, 지수값: {}", 
                                indexName, targetDate, indexData.getClpr());
                    } else {
                        log.debug("시장 지수 데이터 없음 - 지수명: {}, 날짜: {}", indexName, targetDate);
                    }
                    
                    // API 요청 간격 조절 (과도한 요청 방지)
                    Thread.sleep(50);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ExternalApiException("시장 지수 조회 중 인터럽트 발생: " + e.getMessage());
                } catch (Exception e) {
                    log.warn("시장 지수 조회 실패 - 지수명: {}, 날짜: {}, 오류: {}", 
                            indexName, targetDate, e.getMessage());
                    // 개별 지수 조회 실패 시에도 다른 지수는 계속 조회
                }
            }
            
            // 하나라도 유효한 데이터가 있으면 반환
            if (hasValidData && !allIndices.isEmpty()) {
                log.info("시장 지수 조회 완료 - 총 {} 개 지수, 날짜: {}", allIndices.size(), targetDate);
                return allIndices;
            }
            
            log.info("날짜 {} 의 데이터가 없음, 이전 날짜 시도", targetDate);
        }
        
        throw new ExternalApiException("최근 10일 내 시장 지수 데이터를 찾을 수 없습니다");
    }

    private FssMarketIndexResponse.MarketIndexItem getMarketIndexByName(String indexName, LocalDate date) {
        try {
            String url = buildRequestUrl(indexName, date);
            
            ResponseEntity<FssMarketIndexResponse> response = restTemplate.getForEntity(url, FssMarketIndexResponse.class);
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExternalApiException("FSS 시장 지수 API 응답이 유효하지 않습니다");
            }

            FssMarketIndexResponse apiResponse = response.getBody();
            validateApiResponse(apiResponse);

            List<FssMarketIndexResponse.MarketIndexItem> items = extractMarketIndexItems(apiResponse);
            
            // 데이터가 없으면 null 반환 (totalCount가 0이거나 items가 비어있는 경우)
            if (items.isEmpty()) {
                log.debug("시장 지수 데이터 없음 - 지수명: {}, 날짜: {}", indexName, date);
                return null;
            }
            
            // 정확한 지수명 매칭
            return items.stream()
                    .filter(item -> indexName.equals(item.getIdxNm()))
                    .findFirst()
                    .orElse(null);
            
        } catch (RestClientException e) {
            log.error("FSS 시장 지수 API 호출 실패 - 지수명: {}, 날짜: {}", indexName, date, e);
            throw new ExternalApiException("FSS 시장 지수 API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    private String buildRequestUrl(String indexName, LocalDate date) {
        return UriComponentsBuilder.fromHttpUrl(fssApiProperties.getMarketIndexBaseUrl())
                .path("/getStockMarketIndex")
                .queryParam("serviceKey", fssApiProperties.getServiceKey())
                .queryParam("numOfRows", 10) // 여러 결과를 받아서 정확한 지수명 매칭
                .queryParam("pageNo", 1)
                .queryParam("resultType", "json")
                .queryParam("basDt", date.format(DATE_FORMATTER))
                .queryParam("idxNm", indexName)
                .build()
                .toUriString();
    }

    private void validateApiResponse(FssMarketIndexResponse response) {
        if (response.getResponse() == null) {
            throw new ExternalApiException("FSS 시장 지수 API 응답 형식이 올바르지 않습니다");
        }

        FssMarketIndexResponse.Header header = response.getResponse().getHeader();
        if (header == null || !"00".equals(header.getResultCode())) {
            String errorMsg = header != null ? header.getResultMsg() : "알 수 없는 오류";
            throw new ExternalApiException("FSS 시장 지수 API 오류: " + errorMsg);
        }
    }

    private List<FssMarketIndexResponse.MarketIndexItem> extractMarketIndexItems(FssMarketIndexResponse response) {
        FssMarketIndexResponse.Body body = response.getResponse().getBody();
        if (body == null || body.getItems() == null || body.getItems().getItem() == null) {
            return new ArrayList<>();
        }
        
        return body.getItems().getItem();
    }
}