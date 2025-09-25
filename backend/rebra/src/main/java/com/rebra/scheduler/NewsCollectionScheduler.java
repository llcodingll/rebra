package com.rebra.scheduler;

import com.rebra.dto.ai.DeepSearchEconomyResponse;
import com.rebra.entity.News;
import com.rebra.repository.NewsRepository;
import com.rebra.service.ai.DeepSearchService;
import com.rebra.service.ai.FinanceNewsSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsCollectionScheduler {

    private final DeepSearchService deepSearchService;
    private final FinanceNewsSummaryService summaryService;
    private final NewsRepository newsRepository;

    @Scheduled(cron = "0 0 6 * * *") // 매일 오전 6시
    public void collectDailyNews() {
        log.info("=== 일일 뉴스 수집 스케줄러 시작 ===");

        try {
            // 전날 날짜 계산 (서울 시간 기준)
            LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
            String dateString = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            log.info("수집 대상 날짜: {}", dateString);

            // 전날 뉴스 조회 (페이지 크기를 5로 설정)
            DeepSearchEconomyResponse response = deepSearchService.getEconomyNews(dateString, dateString, 5);

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                log.info("조회된 뉴스: {} 건", response.getData().size());
                int savedCount = 0;
                int skipCount = 0;

                for (int i = 0; i < response.getData().size(); i++) {
                    DeepSearchEconomyResponse.Article article = response.getData().get(i);

                    log.info("처리 중: {} / {} - {}", i + 1, response.getData().size(), article.getTitle());

                    try {
                        // 제목과 요약을 합쳐서 AI 요약 요청
                        String fullArticleText = "제목: " + article.getTitle();
                        if (article.getSummary() != null && !article.getSummary().trim().isEmpty()) {
                            fullArticleText += "\n내용: " + article.getSummary();
                        }

                        String aiSummary = null;
                        try {
                            aiSummary = summaryService.summarize(fullArticleText);
                            log.debug("AI 요약 완료: {}", article.getTitle());
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
                        log.debug("DB 저장 완료: ID {} - {}", savedNews.getId(), article.getTitle());

                    } catch (Exception articleError) {
                        skipCount++;
                        log.error("기사 처리 중 오류 발생: {} - {}", article.getTitle(), articleError.getMessage());
                    }
                }

                log.info("=== 뉴스 수집 완료 ===");
                log.info("저장 성공: {} 건", savedCount);
                log.info("처리 실패: {} 건", skipCount);

            } else {
                log.warn("수집된 뉴스가 없습니다. 날짜: {}", dateString);
            }

        } catch (Exception e) {
            log.error("뉴스 수집 스케줄러 실행 중 오류 발생", e);
        }

        log.info("=== 일일 뉴스 수집 스케줄러 종료 ===");
    }
}