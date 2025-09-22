package com.rebra.client;

import com.rebra.config.FssApiProperties;
import com.rebra.dto.external.FssStockBasicInfoResponse;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FssApiClient {

    private final RestTemplate restTemplate;
    private final FssApiProperties fssApiProperties;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<FssStockBasicInfoResponse.StockBasicItem> getStockBasicInfoByName(String stockName) {
        if (!StringUtils.hasText(fssApiProperties.getServiceKey())) {
            throw new ExternalApiException("FSS API 서비스 키가 설정되지 않았습니다");
        }

        try {
            log.info("FSS API 종목기본정보 조회 시작 - 종목명: {}", stockName);
            
            return fetchAllBasicInfoPagesByName(stockName);
            
        } catch (RestClientException e) {
            log.error("FSS API 종목기본정보 호출 실패", e);
            throw new ExternalApiException("FSS API 종목기본정보 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public List<FssStockPriceResponse.StockItem> getStockPriceByNameAndDate(String stockName, LocalDate date) {
        if (!StringUtils.hasText(fssApiProperties.getServiceKey())) {
            throw new ExternalApiException("FSS API 서비스 키가 설정되지 않았습니다");
        }

        try {
            log.info("FSS API 요청 시작 - 종목명: {}, 날짜: {}", stockName, date);
            
            return fetchAllPagesByName(stockName, date);
            
        } catch (RestClientException e) {
            log.error("FSS API 호출 실패", e);
            throw new ExternalApiException("FSS API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public List<FssStockPriceResponse.StockItem> getStockPriceByCodeAndDateRange(String stockCode, LocalDate beginDate, LocalDate endDate) {
        if (!StringUtils.hasText(fssApiProperties.getServiceKey())) {
            throw new ExternalApiException("FSS API 서비스 키가 설정되지 않았습니다");
        }

        try {
            log.info("FSS API 요청 시작 - 종목코드: {}, 시작날짜: {}, 종료날짜: {}", stockCode, beginDate, endDate);
            
            return fetchAllPagesByCode(stockCode, beginDate, endDate);
            
        } catch (RestClientException e) {
            log.error("FSS API 호출 실패", e);
            throw new ExternalApiException("FSS API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private List<FssStockPriceResponse.StockItem> fetchAllPagesByName(String stockName, LocalDate date) {
        List<FssStockPriceResponse.StockItem> allItems = new ArrayList<>();
        int pageNo = 1;
        int numOfRows = 100;
        int totalCount = 0;
        
        while (true) {
            String url = buildRequestUrlForNameSearch(stockName, date, pageNo, numOfRows);
            
            ResponseEntity<FssStockPriceResponse> response = restTemplate.getForEntity(url, FssStockPriceResponse.class);
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExternalApiException("FSS API 응답이 유효하지 않습니다");
            }

            FssStockPriceResponse apiResponse = response.getBody();
            validateApiResponse(apiResponse);

            List<FssStockPriceResponse.StockItem> items = extractStockItems(apiResponse);
            allItems.addAll(items);
            
            if (pageNo == 1) {
                totalCount = apiResponse.getResponse().getBody().getTotalCount();
                log.info("FSS API 전체 데이터 개수: {} 개", totalCount);
                
                if (totalCount == 0) {
                    break;
                }
            }
            
            log.info("FSS API 페이지 {} 처리 완료 - 현재 페이지 항목 수: {} 개, 누적 항목 수: {} 개", 
                    pageNo, items.size(), allItems.size());
            
            if (allItems.size() >= totalCount || items.isEmpty()) {
                break;
            }
            
            pageNo++;
        }
        
        log.info("FSS API 응답 완료 - 총 {} 페이지, 전체 조회된 종목 수: {} 개", pageNo, allItems.size());
        return allItems;
    }

    private List<FssStockPriceResponse.StockItem> fetchAllPagesByCode(String stockCode, LocalDate beginDate, LocalDate endDate) {
        List<FssStockPriceResponse.StockItem> allItems = new ArrayList<>();
        int pageNo = 1;
        int numOfRows = 100;
        int totalCount = 0;
        
        while (true) {
            String url = buildRequestUrlForCodeSearch(stockCode, beginDate, endDate, pageNo, numOfRows);
            
            ResponseEntity<FssStockPriceResponse> response = restTemplate.getForEntity(url, FssStockPriceResponse.class);
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExternalApiException("FSS API 응답이 유효하지 않습니다");
            }

            FssStockPriceResponse apiResponse = response.getBody();
            validateApiResponse(apiResponse);

            List<FssStockPriceResponse.StockItem> items = extractStockItems(apiResponse);
            allItems.addAll(items);
            
            if (pageNo == 1) {
                totalCount = apiResponse.getResponse().getBody().getTotalCount();
                log.info("FSS API 전체 데이터 개수: {} 개", totalCount);
                
                if (totalCount == 0) {
                    break;
                }
            }
            
            log.info("FSS API 페이지 {} 처리 완료 - 현재 페이지 항목 수: {} 개, 누적 항목 수: {} 개", 
                    pageNo, items.size(), allItems.size());
            
            if (allItems.size() >= totalCount || items.isEmpty()) {
                break;
            }
            
            pageNo++;
        }
        
        log.info("FSS API 응답 완료 - 총 {} 페이지, 전체 조회된 종목 수: {} 개", pageNo, allItems.size());
        return allItems;
    }

    private String buildRequestUrlForNameSearch(String stockName, LocalDate date, int pageNo, int numOfRows) {
        return UriComponentsBuilder.fromHttpUrl(fssApiProperties.getBaseUrl())
                .path("/getStockPriceInfo")
                .queryParam("serviceKey", fssApiProperties.getServiceKey())
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("resultType", "json")
                .queryParam("basDt", date.format(DATE_FORMATTER))
                .queryParam("likeItmsNm", stockName)
                .build()
                .toUriString();
    }

    private String buildRequestUrlForCodeSearch(String stockCode, LocalDate beginDate, LocalDate endDate, int pageNo, int numOfRows) {
        return UriComponentsBuilder.fromHttpUrl(fssApiProperties.getBaseUrl())
                .path("/getStockPriceInfo")
                .queryParam("serviceKey", fssApiProperties.getServiceKey())
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("resultType", "json")
                .queryParam("beginBasDt", beginDate.format(DATE_FORMATTER))
                .queryParam("endBasDt", endDate.format(DATE_FORMATTER))
                .queryParam("likeSrtnCd", stockCode)
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
            return new ArrayList<>();
        }
        
        return body.getItems().getItem();
    }

    private List<FssStockBasicInfoResponse.StockBasicItem> fetchAllBasicInfoPagesByName(String stockName) {
        // 현재 날짜에서 3일 전 날짜로 기준일자 설정 (API 데이터 갱신 지연 고려)
        LocalDate baseDate = LocalDate.now().minusDays(3);
        
        String url = buildRequestUrlForBasicInfoSearch(stockName, baseDate);
        
        ResponseEntity<FssStockBasicInfoResponse> response = restTemplate.getForEntity(url, FssStockBasicInfoResponse.class);
        
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new ExternalApiException("FSS API 종목기본정보 응답이 유효하지 않습니다");
        }

        FssStockBasicInfoResponse apiResponse = response.getBody();
        validateBasicInfoApiResponse(apiResponse);

        List<FssStockBasicInfoResponse.StockBasicItem> items = extractStockBasicItems(apiResponse);
        
        log.info("FSS API 종목기본정보 조회 완료 - 기준일자: {}, 조회된 종목 수: {} 개", 
                baseDate.format(DATE_FORMATTER), items.size());
        
        return items;
    }

    private String buildRequestUrlForBasicInfoSearch(String stockName, LocalDate baseDate) {
        return UriComponentsBuilder.fromHttpUrl("https://apis.data.go.kr/1160100/service/GetStocIssuInfoService_V2")
                .path("/getItemBasiInfo_V2")
                .queryParam("serviceKey", fssApiProperties.getServiceKey())
                .queryParam("numOfRows", 100)  // 1페이지 최대 100개 조회
                .queryParam("pageNo", 1)       // 첫 번째 페이지만 조회
                .queryParam("resultType", "json")
                .queryParam("basDt", baseDate.format(DATE_FORMATTER))  // 기준일자 추가
                .queryParam("stckIssuCmpyNm", stockName)
                .build()
                .toUriString();
    }

    private void validateBasicInfoApiResponse(FssStockBasicInfoResponse response) {
        if (response.getResponse() == null) {
            throw new ExternalApiException("FSS API 종목기본정보 응답 형식이 올바르지 않습니다");
        }

        FssStockBasicInfoResponse.Header header = response.getResponse().getHeader();
        if (header == null || !"00".equals(header.getResultCode())) {
            String errorMsg = header != null ? header.getResultMsg() : "알 수 없는 오류";
            throw new ExternalApiException("FSS API 종목기본정보 오류: " + errorMsg);
        }
    }

    private List<FssStockBasicInfoResponse.StockBasicItem> extractStockBasicItems(FssStockBasicInfoResponse response) {
        FssStockBasicInfoResponse.Body body = response.getResponse().getBody();
        if (body == null || body.getItems() == null || body.getItems().getItem() == null) {
            return new ArrayList<>();
        }
        
        return body.getItems().getItem();
    }
}