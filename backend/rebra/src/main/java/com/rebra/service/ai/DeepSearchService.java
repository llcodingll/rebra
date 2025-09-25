package com.rebra.service.ai;

import com.rebra.dto.ai.DeepSearchEconomyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSearchService {

    private final RestTemplate restTemplate;

    @Value("${deepsearch.api.key}")
    private String apiKey;

    private final String baseUrl = "https://api-v2.deepsearch.com/v1/articles/economy";

    public DeepSearchEconomyResponse getEconomyNews(String from, String to, int size) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("date_from", from)
                .queryParam("date_to", to)
                .queryParam("page_size", size)
                .queryParam("api_key", apiKey)
                .build()
                .toUriString();

        ResponseEntity<DeepSearchEconomyResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, null, DeepSearchEconomyResponse.class);

        return response.getBody();
    }
}