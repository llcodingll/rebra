package com.rebra.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rebra.config.TestSecurityConfig;
import com.rebra.dto.response.NewsItemResponse;
import com.rebra.dto.response.NewsSearchResponse;
import com.rebra.exception.GlobalExceptionHandler;
import com.rebra.service.NewsProcessingService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class NewsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NewsProcessingService newsProcessingService;

    @InjectMocks
    private NewsController stockNewsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stockNewsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("주식 뉴스 조회 성공")
    void getStockNews_Success() throws Exception {
        String keyword = "삼성전자";
        int display = 10;
        int start = 1;
        String sort = "date";

        List<NewsItemResponse> newsItems = Arrays.asList(
                NewsItemResponse.builder()
                        .id(1L)
                        .title("삼성전자 주가 상승")
                        .summary("삼성전자가 오늘 주가가 상승했습니다.")
                        .originalLink("https://news.naver.com/1")
                        .link("https://news.naver.com/redirect/1")
                        .publishedAt(LocalDateTime.now())
                        .build(),
                NewsItemResponse.builder()
                        .id(2L)
                        .title("삼성전자 실적 발표")
                        .summary("삼성전자가 분기 실적을 발표했습니다.")
                        .originalLink("https://news.naver.com/2")
                        .link("https://news.naver.com/redirect/2")
                        .publishedAt(LocalDateTime.now())
                        .build()
        );

        String overallSummary = "삼성전자 관련 주요 뉴스들입니다.";
        int totalCount = 100;

        NewsSearchResponse result = NewsSearchResponse.builder()
                .overallSummary(overallSummary)
                .newsItems(newsItems)
                .totalCount(totalCount)
                .build();

        given(newsProcessingService.processStockNews(keyword, display, start, sort))
                .willReturn(result);

        mockMvc.perform(get("/api/v1/news")
                        .param("keyword", keyword)
                        .param("display", String.valueOf(display))
                        .param("start", String.valueOf(start))
                        .param("sort", sort))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.overallSummary").value(overallSummary))
                .andExpect(jsonPath("$.data.totalCount").value(totalCount))
                .andExpect(jsonPath("$.data.newsItems").isArray())
                .andExpect(jsonPath("$.data.newsItems.length()").value(2))
                .andExpect(jsonPath("$.data.newsItems[0].title").value("삼성전자 주가 상승"))
                .andExpect(jsonPath("$.data.newsItems[1].title").value("삼성전자 실적 발표"));
    }

    @Test
    @DisplayName("주식 뉴스 조회 - 키워드 없음")
    void getStockNews_MissingKeyword_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/news")
                        .param("display", "10")
                        .param("start", "1")
                        .param("sort", "date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("MISSING_PARAMETER"));
    }

    @Test
    @DisplayName("주식 뉴스 조회 - 빈 키워드")
    void getStockNews_EmptyKeyword_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/news")
                        .param("keyword", "")
                        .param("display", "10")
                        .param("start", "1")
                        .param("sort", "date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_KEYWORD"));
    }

    @Test
    @DisplayName("주식 뉴스 조회 - 잘못된 display 파라미터")
    void getStockNews_InvalidDisplay_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/news")
                        .param("keyword", "삼성전자")
                        .param("display", "101")
                        .param("start", "1")
                        .param("sort", "date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_DISPLAY"));
    }

    @Test
    @DisplayName("주식 뉴스 조회 - 잘못된 start 파라미터")
    void getStockNews_InvalidStart_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/news")
                        .param("keyword", "삼성전자")
                        .param("display", "10")
                        .param("start", "1001")
                        .param("sort", "date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_START"));
    }

    @Test
    @DisplayName("주식 뉴스 조회 - 잘못된 sort 파라미터")
    void getStockNews_InvalidSort_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/news")
                        .param("keyword", "삼성전자")
                        .param("display", "10")
                        .param("start", "1")
                        .param("sort", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_SORT"));
    }

    @Test
    @DisplayName("주식 뉴스 조회 - 기본 파라미터 사용")
    void getStockNews_DefaultParameters_Success() throws Exception {
        String keyword = "애플";

        List<NewsItemResponse> newsItems = Arrays.asList(
                NewsItemResponse.builder()
                        .id(1L)
                        .title("애플 신제품 출시")
                        .summary("애플이 새로운 제품을 출시했습니다.")
                        .originalLink("https://news.naver.com/1")
                        .link("https://news.naver.com/redirect/1")
                        .publishedAt(LocalDateTime.now())
                        .build()
        );

        NewsSearchResponse result = NewsSearchResponse.builder()
                .overallSummary("애플 관련 뉴스")
                .newsItems(newsItems)
                .totalCount(50)
                .build();

        given(newsProcessingService.processStockNews(keyword, 30, 1, "date"))
                .willReturn(result);

        mockMvc.perform(get("/api/v1/news")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.newsItems.length()").value(1));
    }

    @Test
    @DisplayName("주식 뉴스 조회 - 서비스 에러")
    void getStockNews_ServiceError_InternalServerError() throws Exception {
        String keyword = "삼성전자";

        given(newsProcessingService.processStockNews(keyword, 30, 1, "date"))
                .willThrow(new RuntimeException("네이버 API 오류"));

        mockMvc.perform(get("/api/v1/news")
                        .param("keyword", keyword))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NEWS_FETCH_FAILED"));
    }
}