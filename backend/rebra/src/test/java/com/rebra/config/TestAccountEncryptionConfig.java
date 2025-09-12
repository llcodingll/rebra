package com.rebra.config;

import com.rebra.util.AccountEncryptionUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * AccountEncryptionUtil 테스트를 위한 설정 클래스
 * hashPepper 초기화 문제를 해결합니다.
 */
@TestConfiguration
public class TestAccountEncryptionConfig {

    @Bean
    @Primary
    public AccountEncryptionUtil accountEncryptionUtil() {
        AccountEncryptionUtil util = new AccountEncryptionUtil();

        // 테스트용 pepper 값 설정
        ReflectionTestUtils.setField(util, "hashPepperValue", "test-pepper-value-for-unit-test");

        // @PostConstruct 메서드 수동 호출
        util.init();

        return util;
    }
}