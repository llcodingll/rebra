package com.rebra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fss.api")
@Getter
@Setter
public class FssApiProperties {

    private String baseUrl;
    private String marketIndexBaseUrl;
    private String serviceKey;
    private int timeout = 30000; // 30초 기본값
    private int maxRetries = 3;
    private long retryDelay = 1000; // 1초 기본값
}