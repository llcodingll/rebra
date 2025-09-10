package com.rebra.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.rebra.common.CommonApiResponse;
import com.rebra.dto.response.NewsSearchResponse;
import com.rebra.service.NewsProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/news")
@Tag(name = "News API", description = "뉴스 조회 API")
public class NewsController {

    private final NewsProcessingService newsProcessingService;

    @Operation(
            summary = "뉴스 조회",
            description = "지정된 키워드로 네이버 뉴스를 검색하고 GMS를 통해 요약된 결과를 제공합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뉴스 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("")
    public ResponseEntity<CommonApiResponse<NewsSearchResponse>> getStockNews(
            @Parameter(description = "검색할 키워드", required = true, example = "삼성전자")
            @RequestParam String keyword,

            @Parameter(description = "검색 결과 출력 건수 (1~100)", example = "10")
            @RequestParam(defaultValue = "30") int display,

            @Parameter(description = "검색 시작 위치 (1~1000)", example = "1")
            @RequestParam(defaultValue = "1") int start,

            @Parameter(description = "정렬 옵션 (sim: 정확도순, date: 날짜순)", example = "date")
            @RequestParam(defaultValue = "date") String sort
    ) {
        try {
            log.info("뉴스 조회 요청: keyword={}, display={}, start={}, sort={}", keyword, display, start, sort);

            // 파라미터 유효성 검증
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(CommonApiResponse.error("INVALID_KEYWORD", "키워드는 필수입니다.", BAD_REQUEST));
            }

            if (display < 1 || display > 100) {
                return ResponseEntity.badRequest()
                        .body(CommonApiResponse.error("INVALID_DISPLAY", "display는 1~100 사이의 값이어야 합니다.", BAD_REQUEST));
            }

            if (start < 1 || start > 1000) {
                return ResponseEntity.badRequest()
                        .body(CommonApiResponse.error("INVALID_START", "start는 1~1000 사이의 값이어야 합니다.", BAD_REQUEST));
            }

            if (!"sim".equals(sort) && !"date".equals(sort)) {
                return ResponseEntity.badRequest()
                        .body(CommonApiResponse.error("INVALID_SORT", "sort는 'sim' 또는 'date'만 가능합니다.", BAD_REQUEST));
            }

            NewsSearchResponse result =
                    newsProcessingService.processStockNews(keyword.trim(), display, start, sort);

            log.info("뉴스 조회 완료: keyword={}, 총 {}건", keyword, result.getTotalCount());
            return ResponseEntity.ok(CommonApiResponse.success(result));

        } catch (Exception e) {
            log.error("뉴스 조회 중 오류 발생: keyword={}, error={}", keyword, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(CommonApiResponse.error("NEWS_FETCH_FAILED", "뉴스 조회에 실패했습니다.", INTERNAL_SERVER_ERROR));
        }
    }
}