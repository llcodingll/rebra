package com.rebra.config;

import com.rebra.client.FssApiClient;
import com.rebra.dto.response.StockPriceApiResponse;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * FSS API 클라이언트 모킹 설정
 * 테스트에서 외부 API 호출을 모킹하여 안정적인 테스트 환경 제공
 */
@TestConfiguration
public class MockFssApiConfig {

    @MockBean
    @Primary
    private FssApiClient fssApiClient;

    /**
     * 삼성전자 테스트 데이터 설정
     */
    public void setupSamsungTestData() {
        StockPriceApiResponse.Item samsungItem = StockPriceApiResponse.Item.builder()
                .basDt("20240115")  // 2024-01-15
                .srtnCd("005930")   // 삼성전자 종목코드
                .itmsNm("삼성전자")
                .clpr("75000")      // 종가
                .mkp("74500")       // 시가
                .hipr("75500")      // 고가
                .lopr("74000")      // 저가
                .vs("1000")         // 전일대비
                .fltRt("1.35")      // 등락률
                .trqu("12345678")   // 거래량
                .trPrc("925000000000") // 거래대금
                .lstgStCnt("5969782550")  // 상장주식수
                .mrktTotAmt("447234191250000") // 시가총액
                .build();

        StockPriceApiResponse response = StockPriceApiResponse.builder()
                .response(StockPriceApiResponse.Response.builder()
                        .header(StockPriceApiResponse.Header.builder()
                                .resultCode("00")
                                .resultMsg("NORMAL_SERVICE")
                                .build())
                        .body(StockPriceApiResponse.Body.builder()
                                .numOfRows(1)
                                .pageNo(1)
                                .totalCount(1)
                                .items(List.of(samsungItem))
                                .build())
                        .build())
                .build();

        given(fssApiClient.getStockPriceByNameAndDate(
                ArgumentMatchers.eq("삼성전자"),
                ArgumentMatchers.eq(LocalDate.of(2024, 1, 15))
        )).willReturn(List.of(samsungItem));
    }

    /**
     * SK하이닉스 테스트 데이터 설정
     */
    public void setupSKHynixTestData() {
        StockPriceApiResponse.Item skhynixItem = StockPriceApiResponse.Item.builder()
                .basDt("20240115")
                .srtnCd("000660")   // SK하이닉스 종목코드
                .itmsNm("SK하이닉스")
                .clpr("140000")
                .mkp("138000")
                .hipr("142000")
                .lopr("137000")
                .vs("2000")
                .fltRt("1.45")
                .trqu("5432109")
                .trPrc("760000000000")
                .lstgStCnt("728002365")
                .mrktTotAmt("101920331100000")
                .build();

        StockPriceApiResponse response = StockPriceApiResponse.builder()
                .response(StockPriceApiResponse.Response.builder()
                        .header(StockPriceApiResponse.Header.builder()
                                .resultCode("00")
                                .resultMsg("NORMAL_SERVICE")
                                .build())
                        .body(StockPriceApiResponse.Body.builder()
                                .numOfRows(1)
                                .pageNo(1)
                                .totalCount(1)
                                .items(List.of(skhynixItem))
                                .build())
                        .build())
                .build();

        given(fssApiClient.getStockPriceByNameAndDate(
                ArgumentMatchers.eq("SK하이닉스"),
                ArgumentMatchers.eq(LocalDate.of(2024, 1, 15))
        )).willReturn(List.of(skhynixItem));
    }

    /**
     * LG화학 테스트 데이터 설정
     */
    public void setupLGChemTestData() {
        StockPriceApiResponse.Item lgchemItem = StockPriceApiResponse.Item.builder()
                .basDt("20240115")
                .srtnCd("051910")   // LG화학 종목코드
                .itmsNm("LG화학")
                .clpr("420000")
                .mkp("418000")
                .hipr("425000")
                .lopr("415000")
                .vs("5000")
                .fltRt("1.20")
                .trqu("876543")
                .trPrc("368000000000")
                .lstgStCnt("706000000")
                .mrktTotAmt("296520000000000")
                .build();

        StockPriceApiResponse response = StockPriceApiResponse.builder()
                .response(StockPriceApiResponse.Response.builder()
                        .header(StockPriceApiResponse.Header.builder()
                                .resultCode("00")
                                .resultMsg("NORMAL_SERVICE")
                                .build())
                        .body(StockPriceApiResponse.Body.builder()
                                .numOfRows(1)
                                .pageNo(1)
                                .totalCount(1)
                                .items(List.of(lgchemItem))
                                .build())
                        .build())
                .build();

        given(fssApiClient.getStockPriceByNameAndDate(
                ArgumentMatchers.eq("LG화학"),
                ArgumentMatchers.eq(LocalDate.of(2024, 1, 15))
        )).willReturn(List.of(lgchemItem));
    }

    /**
     * 데이터가 없는 경우 빈 응답 설정
     */
    public void setupEmptyResponse(String stockName, LocalDate date) {
        StockPriceApiResponse emptyResponse = StockPriceApiResponse.builder()
                .response(StockPriceApiResponse.Response.builder()
                        .header(StockPriceApiResponse.Header.builder()
                                .resultCode("00")
                                .resultMsg("NORMAL_SERVICE")
                                .build())
                        .body(StockPriceApiResponse.Body.builder()
                                .numOfRows(0)
                                .pageNo(1)
                                .totalCount(0)
                                .items(List.of())
                                .build())
                        .build())
                .build();

        given(fssApiClient.getStockPriceByNameAndDate(
                ArgumentMatchers.eq(stockName),
                ArgumentMatchers.eq(date)
        )).willReturn(List.of());
    }

    /**
     * API 오류 응답 설정
     */
    public void setupErrorResponse(String stockName, LocalDate date) {
        StockPriceApiResponse errorResponse = StockPriceApiResponse.builder()
                .response(StockPriceApiResponse.Response.builder()
                        .header(StockPriceApiResponse.Header.builder()
                                .resultCode("99")
                                .resultMsg("SERVICE_ERROR")
                                .build())
                        .body(null)
                        .build())
                .build();

        given(fssApiClient.getStockPriceByNameAndDate(
                ArgumentMatchers.eq(stockName),
                ArgumentMatchers.eq(date)
        )).willThrow(new RuntimeException("API Error: " + stockName));
    }

    /**
     * 기간별 테스트 데이터 설정 (2024-01-01 ~ 2024-03-31)
     */
    public void setupPeriodTestData() {
        // 삼성전자 기간 데이터
        for (int month = 1; month <= 3; month++) {
            for (int day = 1; day <= 31; day++) {
                try {
                    LocalDate testDate = LocalDate.of(2024, month, day);
                    // 주말 제외 (월~금만)
                    if (testDate.getDayOfWeek().getValue() <= 5) {
                        setupSingleStockData("삼성전자", "005930", testDate, 
                                75000 + (day - 15) * 100); // 가격 변동
                    }
                } catch (Exception e) {
                    // 잘못된 날짜 무시 (예: 2월 30일)
                }
            }
        }

        // SK하이닉스 기간 데이터
        for (int month = 1; month <= 3; month++) {
            for (int day = 1; day <= 31; day++) {
                try {
                    LocalDate testDate = LocalDate.of(2024, month, day);
                    if (testDate.getDayOfWeek().getValue() <= 5) {
                        setupSingleStockData("SK하이닉스", "000660", testDate, 
                                140000 + (day - 15) * 200);
                    }
                } catch (Exception e) {
                    // 잘못된 날짜 무시
                }
            }
        }

        // LG화학 기간 데이터
        for (int month = 1; month <= 3; month++) {
            for (int day = 1; day <= 31; day++) {
                try {
                    LocalDate testDate = LocalDate.of(2024, month, day);
                    if (testDate.getDayOfWeek().getValue() <= 5) {
                        setupSingleStockData("LG화학", "051910", testDate, 
                                420000 + (day - 15) * 500);
                    }
                } catch (Exception e) {
                    // 잘못된 날짜 무시
                }
            }
        }
    }

    /**
     * 단일 종목의 특정 날짜 데이터 설정
     */
    private void setupSingleStockData(String stockName, String stockCode, LocalDate date, int price) {
        StockPriceApiResponse.Item item = StockPriceApiResponse.Item.builder()
                .basDt(date.toString().replace("-", ""))
                .srtnCd(stockCode)
                .itmsNm(stockName)
                .clpr(String.valueOf(price))
                .mkp(String.valueOf(price - 500))
                .hipr(String.valueOf(price + 500))
                .lopr(String.valueOf(price - 1000))
                .vs("500")
                .fltRt("0.67")
                .trqu("1000000")
                .trPrc("1000000000000")
                .lstgStCnt("1000000000")
                .mrktTotAmt("100000000000000")
                .build();

        StockPriceApiResponse response = StockPriceApiResponse.builder()
                .response(StockPriceApiResponse.Response.builder()
                        .header(StockPriceApiResponse.Header.builder()
                                .resultCode("00")
                                .resultMsg("NORMAL_SERVICE")
                                .build())
                        .body(StockPriceApiResponse.Body.builder()
                                .numOfRows(1)
                                .pageNo(1)
                                .totalCount(1)
                                .items(List.of(item))
                                .build())
                        .build())
                .build();

        given(fssApiClient.getStockPriceByNameAndDate(
                ArgumentMatchers.eq(stockName),
                ArgumentMatchers.eq(date)
        )).willReturn(List.of(item));
    }

    /**
     * Mock 객체 반환 (테스트에서 verify 등을 위해 사용)
     */
    public FssApiClient getFssApiClient() {
        return fssApiClient;
    }

    /**
     * 모든 모킹 설정 초기화
     */
    public void resetMocks() {
        reset(fssApiClient);
    }
}