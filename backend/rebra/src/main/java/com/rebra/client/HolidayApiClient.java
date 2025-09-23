package com.rebra.client;

import com.rebra.config.FssApiProperties;
import com.rebra.dto.external.HolidayApiResponse;
import com.rebra.exception.external.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayApiClient {

    private final RestTemplate restTemplate;
    private final FssApiProperties fssApiProperties;

    /**
     * 특정 연/월의 공휴일 날짜 목록을 조회한다 (캐싱 적용)
     * 
     * @param year 연도
     * @param month 월
     * @return 공휴일 날짜 Set (LocalDate)
     */
    @Cacheable(value = "holidayCache", key = "#year + '-' + #month")
    public Set<LocalDate> getHolidaysForMonth(int year, int month) {
        if (!StringUtils.hasText(fssApiProperties.getServiceKey())) {
            throw new ExternalApiException("공휴일 API 서비스 키가 설정되지 않았습니다");
        }

        try {
            log.info("공휴일 API 요청 시작 - 연도: {}, 월: {}", year, month);
            
            String url = buildRequestUrl(year, month, 1, 100);
            
            ResponseEntity<HolidayApiResponse> response = restTemplate.getForEntity(url, HolidayApiResponse.class);
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExternalApiException("공휴일 API 응답이 유효하지 않습니다");
            }

            HolidayApiResponse apiResponse = response.getBody();
            validateApiResponse(apiResponse);

            List<HolidayApiResponse.HolidayItem> holidayItems = extractHolidayItems(apiResponse);
            
            Set<LocalDate> holidays = holidayItems.stream()
                    .filter(item -> "Y".equals(item.getIsHoliday()))
                    .map(item -> parseDate(item.getLocdate()))
                    .filter(date -> date != null)
                    .collect(Collectors.toSet());
                    
            log.info("공휴일 API 응답 완료 - {}년 {}월 공휴일: {} 개", year, month, holidays.size());
            return holidays;
            
        } catch (RestClientException e) {
            log.error("공휴일 API 호출 실패", e);
            throw new ExternalApiException("공휴일 API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }


    private String buildRequestUrl(int year, int month, int pageNo, int numOfRows) {
        return UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService")
                .path("/getRestDeInfo")
                .queryParam("serviceKey", fssApiProperties.getServiceKey())
                .queryParam("solYear", String.valueOf(year))
                .queryParam("solMonth", String.format("%02d", month))
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("_type", "json")
                .build()
                .toUriString();
    }

    private void validateApiResponse(HolidayApiResponse response) {
        if (response.getResponse() == null) {
            throw new ExternalApiException("공휴일 API 응답 형식이 올바르지 않습니다");
        }

        HolidayApiResponse.Header header = response.getResponse().getHeader();
        if (header == null || !"00".equals(header.getResultCode())) {
            String errorMsg = header != null ? header.getResultMsg() : "알 수 없는 오류";
            throw new ExternalApiException("공휴일 API 오류: " + errorMsg);
        }
    }

    private List<HolidayApiResponse.HolidayItem> extractHolidayItems(HolidayApiResponse response) {
        HolidayApiResponse.Body body = response.getResponse().getBody();
        if (body == null || body.getItems() == null) {
            return new ArrayList<>();
        }
        
        List<HolidayApiResponse.HolidayItem> items = body.getItems().getItem();
        if (items == null) {
            return new ArrayList<>();
        }
        
        return items;
    }

    private LocalDate parseDate(String dateStr) {
        try {
            if (dateStr != null && dateStr.length() == 8) {
                int year = Integer.parseInt(dateStr.substring(0, 4));
                int month = Integer.parseInt(dateStr.substring(4, 6));
                int day = Integer.parseInt(dateStr.substring(6, 8));
                return LocalDate.of(year, month, day);
            }
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateStr, e);
        }
        return null;
    }
}