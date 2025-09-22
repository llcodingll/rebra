package com.rebra.service;

import com.rebra.client.FssMarketIndexApiClient;
import com.rebra.dto.external.FssMarketIndexResponse;
import com.rebra.dto.response.MarketIndexResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketIndexServiceImpl implements MarketIndexService {

    private final FssMarketIndexApiClient fssMarketIndexApiClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Cacheable(value = "marketIndexCache", key = "'market_indices_' + T(java.time.LocalDate).now().format(T(java.time.format.DateTimeFormatter).ofPattern('yyyy-MM-dd'))")
    public MarketIndexResponse getLatestMarketIndices() {
        log.info("시장 지수 조회 시작");
        
        List<FssMarketIndexResponse.MarketIndexItem> apiData = fssMarketIndexApiClient.getLatestMarketIndices();
        
        if (apiData.isEmpty()) {
            log.warn("시장 지수 데이터가 없습니다");
            return MarketIndexResponse.builder()
                    .requestDate(LocalDate.now())
                    .indices(List.of())
                    .build();
        }

        // 첫 번째 데이터에서 실제 데이터 날짜 추출
        LocalDate dataDate = parseDataDate(apiData.get(0).getBasDt());
        
        List<MarketIndexResponse.MarketIndexInfo> indices = apiData.stream()
                .map(this::convertToMarketIndexInfo)
                .collect(Collectors.toList());

        MarketIndexResponse response = MarketIndexResponse.builder()
                .requestDate(LocalDate.now())
                .dataDate(dataDate)
                .indices(indices)
                .build();

        log.info("시장 지수 조회 완료 - 요청일: {}, 데이터일: {}, 지수 개수: {}", 
                response.getRequestDate(), response.getDataDate(), indices.size());
        
        return response;
    }

    private MarketIndexResponse.MarketIndexInfo convertToMarketIndexInfo(FssMarketIndexResponse.MarketIndexItem item) {
        return MarketIndexResponse.MarketIndexInfo.builder()
                .indexName(item.getIdxNm())
                .indexCode(generateIndexCode(item.getIdxNm()))
                .currentPrice(item.getClpr())
                .changeAmount(item.getVs())
                .changeRate(item.getFltRt())
                .openPrice(item.getMkp())
                .highPrice(item.getHipr())
                .lowPrice(item.getLopr())
                .tradingVolume(item.getTrqu())
                .tradingValue(item.getTrPrc())
                .marketCap(item.getLstgMrktTotAmt())
                .listedStockCount(item.getEpyItmsCnt())
                .yearHighPrice(item.getYrWRcrdHgst())
                .yearHighDate(formatDate(item.getYrWRcrdHgstDt()))
                .yearLowPrice(item.getYrWRcrdLwst())
                .yearLowDate(formatDate(item.getYrWRcrdLwstDt()))
                .build();
    }

    private String generateIndexCode(String indexName) {
        if (indexName == null) return "UNKNOWN";
        
        switch (indexName) {
            case "코스피":
                return "KOSPI";
            case "코스피 200":
                return "KOSPI200";
            case "KRX 300":
                return "KRX300";
            case "코스닥":
                return "KOSDAQ";
            default:
                return indexName.replaceAll("\\s+", "").toUpperCase();
        }
    }

    private LocalDate parseDataDate(String baseDt) {
        if (!StringUtils.hasText(baseDt) || baseDt.length() != 8) {
            log.warn("유효하지 않은 기준일자: {}, 현재 날짜 사용", baseDt);
            return LocalDate.now();
        }
        
        try {
            return LocalDate.parse(baseDt, DATE_FORMATTER);
        } catch (Exception e) {
            log.error("기준일자 파싱 실패: {}, 현재 날짜 사용", baseDt, e);
            return LocalDate.now();
        }
    }

    private String formatDate(String dateStr) {
        if (!StringUtils.hasText(dateStr) || dateStr.length() != 8) {
            return dateStr;
        }
        
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            log.warn("날짜 포맷 변환 실패: {}", dateStr);
            return dateStr;
        }
    }
}