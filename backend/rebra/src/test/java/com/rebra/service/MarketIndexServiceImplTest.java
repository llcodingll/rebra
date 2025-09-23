package com.rebra.service;

import com.rebra.client.FssMarketIndexApiClient;
import com.rebra.dto.external.FssMarketIndexResponse;
import com.rebra.dto.response.MarketIndexResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketIndexServiceImplTest {

    @Mock
    private FssMarketIndexApiClient fssMarketIndexApiClient;

    @InjectMocks
    private MarketIndexServiceImpl marketIndexService;

    private FssMarketIndexResponse.MarketIndexItem kospiItem;
    private FssMarketIndexResponse.MarketIndexItem kosdaqItem;

    @BeforeEach
    void setUp() {
        kospiItem = new FssMarketIndexResponse.MarketIndexItem();
        kospiItem.setBasDt("20240920");
        kospiItem.setIdxNm("코스피");
        kospiItem.setClpr("2770.69");
        kospiItem.setVs("32.5");
        kospiItem.setFltRt("1.19");
        kospiItem.setMkp("2745.58");
        kospiItem.setHipr("2770.7");
        kospiItem.setLopr("2733.63");
        kospiItem.setTrqu("557090057");
        kospiItem.setTrPrc("12197991898146");
        kospiItem.setLstgMrktTotAmt("2262832341048634");
        kospiItem.setEpyItmsCnt(839);
        kospiItem.setYrWRcrdHgst("2891.35");
        kospiItem.setYrWRcrdHgstDt("20240711");
        kospiItem.setYrWRcrdLwst("2233.07");
        kospiItem.setYrWRcrdLwstDt("20240108");

        kosdaqItem = new FssMarketIndexResponse.MarketIndexItem();
        kosdaqItem.setBasDt("20240920");
        kosdaqItem.setIdxNm("코스닥");
        kosdaqItem.setClpr("770.25");
        kosdaqItem.setVs("-5.23");
        kosdaqItem.setFltRt("-0.67");
        kosdaqItem.setMkp("772.15");
        kosdaqItem.setHipr("773.82");
        kosdaqItem.setLopr("768.91");
        kosdaqItem.setTrqu("123456789");
        kosdaqItem.setTrPrc("987654321098");
        kosdaqItem.setLstgMrktTotAmt("456789123456789");
        kosdaqItem.setEpyItmsCnt(1245);
        kosdaqItem.setYrWRcrdHgst("850.12");
        kosdaqItem.setYrWRcrdHgstDt("20240315");
        kosdaqItem.setYrWRcrdLwst("680.45");
        kosdaqItem.setYrWRcrdLwstDt("20240201");
    }

    @Test
    @DisplayName("시장 지수 조회 성공")
    void getLatestMarketIndices_Success() {
        // Given
        List<FssMarketIndexResponse.MarketIndexItem> mockData = Arrays.asList(kospiItem, kosdaqItem);
        when(fssMarketIndexApiClient.getLatestMarketIndices()).thenReturn(mockData);

        // When
        MarketIndexResponse response = marketIndexService.getLatestMarketIndices();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRequestDate()).isEqualTo(LocalDate.now());
        assertThat(response.getDataDate()).isEqualTo(LocalDate.of(2024, 9, 20));
        assertThat(response.getIndices()).hasSize(2);

        MarketIndexResponse.MarketIndexInfo kospiInfo = response.getIndices().get(0);
        assertThat(kospiInfo.getIndexName()).isEqualTo("코스피");
        assertThat(kospiInfo.getIndexCode()).isEqualTo("KOSPI");
        assertThat(kospiInfo.getCurrentPrice()).isEqualTo("2770.69");
        assertThat(kospiInfo.getChangeAmount()).isEqualTo("32.5");
        assertThat(kospiInfo.getChangeRate()).isEqualTo("1.19");

        MarketIndexResponse.MarketIndexInfo kosdaqInfo = response.getIndices().get(1);
        assertThat(kosdaqInfo.getIndexName()).isEqualTo("코스닥");
        assertThat(kosdaqInfo.getIndexCode()).isEqualTo("KOSDAQ");
        assertThat(kosdaqInfo.getCurrentPrice()).isEqualTo("770.25");
        assertThat(kosdaqInfo.getChangeAmount()).isEqualTo("-5.23");
        assertThat(kosdaqInfo.getChangeRate()).isEqualTo("-0.67");
    }

    @Test
    @DisplayName("데이터가 없는 경우 빈 리스트 반환")
    void getLatestMarketIndices_EmptyData() {
        // Given
        when(fssMarketIndexApiClient.getLatestMarketIndices()).thenReturn(Collections.emptyList());

        // When
        MarketIndexResponse response = marketIndexService.getLatestMarketIndices();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRequestDate()).isEqualTo(LocalDate.now());
        assertThat(response.getDataDate()).isNull();
        assertThat(response.getIndices()).isEmpty();
    }

    @Test
    @DisplayName("지수 코드 생성 테스트")
    void generateIndexCode_Test() {
        // Given
        FssMarketIndexResponse.MarketIndexItem kospi200Item = new FssMarketIndexResponse.MarketIndexItem();
        kospi200Item.setBasDt("20240920");
        kospi200Item.setIdxNm("코스피 200");
        kospi200Item.setClpr("320.50");

        FssMarketIndexResponse.MarketIndexItem krx300Item = new FssMarketIndexResponse.MarketIndexItem();
        krx300Item.setBasDt("20240920");
        krx300Item.setIdxNm("KRX 300");
        krx300Item.setClpr("1150.25");

        List<FssMarketIndexResponse.MarketIndexItem> mockData = Arrays.asList(kospi200Item, krx300Item);
        when(fssMarketIndexApiClient.getLatestMarketIndices()).thenReturn(mockData);

        // When
        MarketIndexResponse response = marketIndexService.getLatestMarketIndices();

        // Then
        assertThat(response.getIndices()).hasSize(2);
        assertThat(response.getIndices().get(0).getIndexCode()).isEqualTo("KOSPI200");
        assertThat(response.getIndices().get(1).getIndexCode()).isEqualTo("KRX300");
    }

    @Test
    @DisplayName("날짜 포맷 변환 테스트")
    void dateFormatConversion_Test() {
        // Given
        kospiItem.setYrWRcrdHgstDt("20240315");
        kospiItem.setYrWRcrdLwstDt("20240108");

        when(fssMarketIndexApiClient.getLatestMarketIndices()).thenReturn(Arrays.asList(kospiItem));

        // When
        MarketIndexResponse response = marketIndexService.getLatestMarketIndices();

        // Then
        MarketIndexResponse.MarketIndexInfo indexInfo = response.getIndices().get(0);
        assertThat(indexInfo.getYearHighDate()).isEqualTo("2024-03-15");
        assertThat(indexInfo.getYearLowDate()).isEqualTo("2024-01-08");
    }
}