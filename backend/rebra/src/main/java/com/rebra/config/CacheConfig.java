package com.rebra.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Caffeine 캐시 매니저 설정
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 공휴일 캐시 설정
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(24)  // 최대 24개월치 데이터 (2년)
                .expireAfterWrite(30, TimeUnit.DAYS)  // 30일 후 만료
                .recordStats()  // 캐시 통계 기록
        );
        
        // 캐시 이름 등록
        cacheManager.setCacheNames(java.util.Arrays.asList("holidayCache"));
        
        return cacheManager;
    }
}