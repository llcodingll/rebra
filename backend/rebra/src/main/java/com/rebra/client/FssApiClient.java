package com.rebra.client;

import com.rebra.config.FssApiProperties;
import com.rebra.dto.external.FssStockPriceResponse;
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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FssApiClient {

    private final RestTemplate restTemplate;
    private final FssApiProperties fssApiProperties;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<FssStockPriceResponse.StockItem> getStockPriceByNameAndDate(String stockName, LocalDate date) {
        if (!StringUtils.hasText(fssApiProperties.getServiceKey())) {
            throw new ExternalApiException("FSS API 서비스 키가 설정되지 않았습니다");
        }

        String url = buildRequestUrl(stockName, date);
        
        try {
            log.info("FSS API 요청 시작 - 종목명: {}, 날짜: {}", stockName, date);
            
            ResponseEntity<FssStockPriceResponse> response = restTemplate.getForEntity(url, FssStockPriceResponse.class);
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExternalApiException("FSS API 응답이 유효하지 않습니다");
            }

            FssStockPriceResponse apiResponse = response.getBody();
            validateApiResponse(apiResponse);

            List<FssStockPriceResponse.StockItem> items = extractStockItems(apiResponse);
            log.info("FSS API 응답 성공 - 조회된 종목 수: {}", items.size());
            
            return items;
            
        } catch (RestClientException e) {
            log.error("FSS API 호출 실패", e);
            throw new ExternalApiException("FSS API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String buildRequestUrl(String stockName, LocalDate date) {
        return UriComponentsBuilder.fromHttpUrl(fssApiProperties.getBaseUrl())
                .path("/getStockPriceInfo")
                .queryParam("serviceKey", fssApiProperties.getServiceKey())
                .queryParam("numOfRows", 100) // 최대 100개 조회
                .queryParam("pageNo", 1)
                .queryParam("resultType", "json")
                .queryParam("basDt", date.format(DATE_FORMATTER))
                .queryParam("likeItmsNm", stockName) // 종목명 부분 검색
                .build()
                .toUriString();
    }

    private void validateApiResponse(FssStockPriceResponse response) {
        if (response.getResponse() == null) {
            throw new ExternalApiException("FSS API 응답 형식이 올바르지 않습니다");
        }

        FssStockPriceResponse.Header header = response.getResponse().getHeader();
        if (header == null || !"00".equals(header.getResultCode())) {
            String errorMsg = header != null ? header.getResultMsg() : "알 수 없는 오류";
            throw new ExternalApiException("FSS API 오류: " + errorMsg);
        }
    }

    private List<FssStockPriceResponse.StockItem> extractStockItems(FssStockPriceResponse response) {
        FssStockPriceResponse.Body body = response.getResponse().getBody();
        if (body == null || body.getItems() == null || body.getItems().getItem() == null) {
            log.warn("FSS API에서 데이터를 찾을 수 없습니다");
            return List.of();
        }
        
        return body.getItems().getItem();
    }
}