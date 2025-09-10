package com.rebra.calculator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * 주식 리밸런싱 백테스트 계산 서버 메인 애플리케이션 클래스
 * 
 * 이 서버는 메인 서버로부터 백테스트 요청을 받아 계산을 수행하고 결과를 반환하는 
 * 계산 전용 서버입니다.
 * 
 * 주요 기능:
 * - Kafka를 통한 비동기 메시지 처리
 * - 포트폴리오 리밸런싱 백테스트 계산
 * - 다양한 리밸런싱 전략 지원 (임계값 기반, 주기적)
 * - 거래비용 및 차입비용 반영
 * - 상세한 성과 분석 및 리포트 생성
 */
@Slf4j
@EnableKafka
@SpringBootApplication
public class RebraCalServerApplication {

    /**
     * 애플리케이션 시작점
     * 
     * @param args 명령행 인수
     */
    public static void main(String[] args) {
        try {
            log.info("=== 주식 리밸런싱 백테스트 계산 서버 시작 ===");
            log.info("서버 기능:");
            log.info("  - 포트폴리오 리밸런싱 백테스트 계산");
            log.info("  - 임계값 기반 / 주기적 리밸런싱 전략 지원");
            log.info("  - 거래비용 및 차입비용 반영");
            log.info("  - Kafka 기반 비동기 메시지 처리");
            log.info("  - 상세한 성과 분석 및 리포트");
            
            SpringApplication.run(RebraCalServerApplication.class, args);
            
            log.info("=== 백테스트 계산 서버 시작 완료 ===");
            
        } catch (Exception e) {
            log.error("애플리케이션 시작 중 오류 발생", e);
            System.exit(1);
        }
    }
}