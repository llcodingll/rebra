package com.rebra.controller;

import com.rebra.common.CommonApiResponse;
import com.rebra.dto.ai.DeepSearchEconomyResponse;
import com.rebra.dto.response.NewsListResponse;
import com.rebra.dto.response.NewsResponse;
import com.rebra.entity.News;
import com.rebra.repository.NewsRepository;
import com.rebra.service.ai.DeepSearchService;
import com.rebra.service.ai.FinanceNewsSummaryService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "News API", description = "뉴스 관련 API")
public class NewsController {

    private final DeepSearchService deepSearchService;
    private final FinanceNewsSummaryService summaryService;
    private final NewsRepository newsRepository;

    @Operation(summary = "최신 경제 뉴스 조회", description = "AI로 요약된 최신 경제 뉴스 5개를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "뉴스 조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/latest")
    public ResponseEntity<CommonApiResponse<NewsListResponse>> getLatestNews() {
        try {
            log.info("최신 뉴스 5개 조회 시작");

            List<News> latestNews = newsRepository.findTop5ByOrderByIdDesc();

            List<NewsResponse> newsResponses = latestNews.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            NewsListResponse response = NewsListResponse.from(newsResponses);

            log.info("조회된 뉴스: {} 건", response.getTotalCount());

            return ResponseEntity.ok(CommonApiResponse.success(response));

        } catch (Exception e) {
            log.error("뉴스 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(CommonApiResponse.error("NEWS_001", "뉴스 조회 중 오류가 발생했습니다.",
                            org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Operation(summary = "어제 뉴스 수동 수집", description = "어제 날짜의 경제 뉴스를 DeepSearch API로 수집하여 DB에 저장합니다.")
    @ApiResponse(responseCode = "200", description = "뉴스 수집 성공")
    @PostMapping("/collect/yesterday")
    public ResponseEntity<String> collectYesterdayNews() {
        try {
            log.info("=== 어제 뉴스 수동 수집 시작 ===");

            // 어제 날짜 계산 (서울 시간 기준)
            LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
            String dateString = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            log.info("수집 대상 날짜: {}", dateString);

            // DeepSearch API 호출
            DeepSearchEconomyResponse response = deepSearchService.getEconomyNews(dateString, dateString, 5);

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                log.info("조회된 뉴스: {} 건", response.getData().size());
                int savedCount = 0;
                int skipCount = 0;

                for (int i = 0; i < response.getData().size(); i++) {
                    DeepSearchEconomyResponse.Article article = response.getData().get(i);

                    log.info("처리 중: {} / {} - {}", i + 1, response.getData().size(), article.getTitle());

                    try {
                        // AI 요약 생성
                        String fullArticleText = "제목: " + article.getTitle();
                        if (article.getSummary() != null && !article.getSummary().trim().isEmpty()) {
                            fullArticleText += "\n내용: " + article.getSummary();
                        }

                        String aiSummary = null;
                        try {
                            aiSummary = summaryService.summarize(fullArticleText);
                            log.debug("🤖 AI 요약 완료: {}", article.getTitle());
                        } catch (Exception aiError) {
                            log.warn("AI 요약 실패 (원본 사용): {} - {}", article.getTitle(), aiError.getMessage());
                            aiSummary = article.getSummary();
                        }

                        // 발행일 파싱
                        LocalDateTime publishedAt = null;
                        if (article.getPublished_at() != null) {
                            try {
                                publishedAt = LocalDateTime.parse(article.getPublished_at());
                            } catch (Exception parseError) {
                                log.warn("날짜 파싱 실패로 기사 건너뜀: {} - {}", article.getPublished_at(), parseError.getMessage());
                                skipCount++;
                                continue; // 이 기사는 저장하지 않고 다음 기사로
                            }
                        } else {
                            log.warn("발행일이 없어서 기사 건너뜀: {}", article.getTitle());
                            skipCount++;
                            continue; // 이 기사는 저장하지 않고 다음 기사로
                        }

                        // 중복 체크
                        if (newsRepository.existsByTitleAndUrl(article.getTitle(), article.getContent_url())) {
                            log.info("중복된 뉴스로 건너뜀: {}", article.getTitle());
                            skipCount++;
                            continue;
                        }

                        // DB 저장
                        News news = News.builder()
                                .title(article.getTitle())
                                .summary(aiSummary)
                                .url(article.getContent_url())
                                .imageUrl(article.getImage_url())
                                .publishedAt(publishedAt)
                                .build();

                        News savedNews = newsRepository.save(news);
                        savedCount++;
                        log.debug("💾 DB 저장 완료: ID {} - {}", savedNews.getId(), article.getTitle());

                    } catch (Exception articleError) {
                        skipCount++;
                        log.error("기사 처리 중 오류 발생: {} - {}", article.getTitle(), articleError.getMessage());
                    }
                }

                log.info("=== 어제 뉴스 수집 완료 ===");
                log.info("저장 성공: {} 건", savedCount);
                log.info("처리 실패: {} 건", skipCount);

                return ResponseEntity.ok(String.format("어제(%s) 뉴스 수집이 완료되었습니다. 성공: %d건, 실패: %d건",
                    dateString, savedCount, skipCount));

            } else {
                log.warn("수집된 뉴스가 없습니다. 날짜: {}", dateString);
                return ResponseEntity.ok(String.format("어제(%s) 뉴스가 없습니다.", dateString));
            }

        } catch (Exception e) {
            log.error("어제 뉴스 수집 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("뉴스 수집 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private NewsResponse convertToResponse(News news) {
        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .summary(news.getSummary())
                .url(news.getUrl())
                .imageUrl(news.getImageUrl())
                .publishedAt(news.getPublishedAt())
                .build();
    }
}