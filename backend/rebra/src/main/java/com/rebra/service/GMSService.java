package com.rebra.service;

import com.rebra.config.GMSConfig;
import com.rebra.dto.NaverNewsDto;
import com.rebra.dto.request.ChatMessage;
import com.rebra.dto.request.GMSRequest;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GMSService {

    private final WebClient webClient;
    private final GMSConfig gmsConfig;

    public String summarizeNews(NaverNewsDto naverNewsDto) {
        log.info("GMS 뉴스 요약 시작: 총 {}건의 뉴스", naverNewsDto.getItems().size());

        try {
            String prompt = createSummaryPrompt(naverNewsDto);

            String summary = webClient
                    .post()
                    .uri(gmsConfig.getGptUrl())
                    .header("Authorization", "Bearer " + gmsConfig.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createGMSRequest(prompt))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("GMS 뉴스 요약 완료");
            return summary;

        } catch (Exception e) {
            log.error("GMS 뉴스 요약 중 오류 발생: error={}", e.getMessage(), e);
            throw new RuntimeException("GMS 뉴스 요약 실패: " + e.getMessage(), e);
        }
    }

    private String createSummaryPrompt(NaverNewsDto naverNewsDto) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 뉴스들을 요약해주세요:\n\n");

        naverNewsDto.getItems().forEach(item -> {
            prompt.append("제목: ").append(item.getTitle()).append("\n");
            prompt.append("내용: ").append(item.getDescription()).append("\n");
            prompt.append("발행일: ").append(item.getPubDate()).append("\n\n");
        });

        prompt.append("위 뉴스들의 주요 내용을 3-5줄로 요약해주세요.");
        return prompt.toString();
    }

    private GMSRequest createGMSRequest(String prompt) {
        ChatMessage message = ChatMessage.builder()
                .role("user")
                .content(prompt)
                .build();
                
        return GMSRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(message))
                .build();
    }
}