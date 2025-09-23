package com.rebra.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 통합 캐시 매니저 설정 - 각 캐시별로 다른 설정 적용
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        List<Cache> caches = Arrays.asList(
            // 공휴일 캐시 - 30일 유지, 최대 24개월치 데이터
            new CaffeineCache("holidayCache",
                Caffeine.newBuilder()
                    .maximumSize(24)
                    .expireAfterWrite(30, TimeUnit.DAYS)
                    .recordStats()
                    .build()),
            
            // 시장 지수 캐시 - 1일 유지, 최대 30일치 데이터
            new CaffeineCache("marketIndexCache",
                Caffeine.newBuilder()
                    .maximumSize(30)
                    .expireAfterWrite(1, TimeUnit.DAYS)
                    .recordStats()
                    .build())
        );
        
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}