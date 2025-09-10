# 주식 리밸런싱 백테스트 계산 서버

## 프로젝트 개요

이 프로젝트는 주식 포트폴리오의 리밸런싱 백테스트를 수행하는 계산 전용 서버입니다.
메인 서버로부터 주식 데이터와 포트폴리오 설정을 받아 특정 기간의 리밸런싱을 모의로 수행하고, 그 결과를 반환합니다.

### 주요 특징
- **계산 전용**: DB 접근 없이 순수 계산만 수행
- **비동기 처리**: Kafka를 통한 메시지 기반 통신
- **확장성**: 계산 서버만 독립적으로 스케일아웃 가능
- **장애 격리**: 메인 서버와 분리된 독립적인 서비스

## 시스템 아키텍처

```
메인 서버 → Kafka Topic (backtest-request) → 계산 서버
메인 서버 ← Kafka Topic (backtest-result) ← 계산 서버
```

### 데이터 흐름
1. 메인 서버가 백테스트 요청을 Kafka로 전송
2. 계산 서버가 요청을 수신하여 백테스트 수행
3. 계산 완료 후 결과를 Kafka로 전송
4. 메인 서버가 결과를 수신하여 DB에 저장

## 데이터 모델

### 요청 메시지 (BacktestRequest)
```json
{
  "backtest_id": 123,
  "user_id": 456,
  "test_name": "포트폴리오 백테스트",
  "start_date": "2023-01-01",
  "end_date": "2023-12-31",
  "initial_capital": 10000000,
  "rebalancing_type": "THRESHOLD",
  "rebalancing_period": "MONTHLY",
  "stocks": [
    {
      "stock_code": "005930",
      "stock_name": "삼성전자",
      "target_weight": 0.4,
      "threshold_percentage": 0.05
    }
  ],
  "ohlcv_data": [
    {
      "stock_code": "005930",
      "trade_date": "2023-01-01",
      "close_price": 60000
    }
  ]
}
```

### 응답 메시지 (BacktestResponse)
```json
{
  "backtest_id": 123,
  "status": "COMPLETED",
  "summary": {
    "final_value": 12500000,
    "total_return": 0.25,
    "buy_hold_return": 0.20,
    "excess_return": 0.05,
    "period_growth_rate": 0.023,
    "rebalancing_count": 12,
    "total_fee": 150000,
    "win_rate": 0.75
  },
  "details": [
    {
      "period_date": "2023-01-31",
      "portfolio_value": 10200000,
      "period_return": 0.02,
      "is_rebalanced": true
    }
  ]
}
```

## Kafka 설정

### Topic 설정
- **backtest-request**: 백테스트 계산 요청
- **backtest-result**: 백테스트 계산 결과

### Consumer 설정
```yaml
spring:
  kafka:
    consumer:
      group-id: backtest-calculator
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
```

## 핵심 계산 로직

### 1. 포트폴리오 가치 계산
- 각 종목의 보유 수량 × 현재가 합계
- 현금 보유액 포함

### 2. 리밸런싱 실행 조건
- **임계값 기반**: 목표 비중에서 임계값(%) 이상 벗어날 때
- **주기 기반**: 설정된 주기(월/분기)마다 무조건 실행

### 3. 매매 수량 계산
```java
// 목표 금액 계산
double targetAmount = portfolioValue * targetWeight;
// 현재 보유 금액
double currentAmount = shares * currentPrice;
// 매매 필요 금액
double tradingAmount = targetAmount - currentAmount;
// 매매 수량 (소수점 이하 버림)
int tradingShares = (int) Math.floor(Math.abs(tradingAmount) / currentPrice);
```

### 4. 거래비용 계산
```java
// 하드코딩된 수수료율 및 세금
private static final double BUY_FEE_RATE = 0.00015;  // 0.015%
private static final double SELL_FEE_RATE = 0.00015; // 0.015%
private static final double TAX_RATE = 0.003;        // 0.3% (매도시만)

// 총 거래비용 = (매수금액 × 매수수수료) + (매도금액 × 매도수수료) + (매도금액 × 세금)
```

## API 엔드포인트

### Kafka Listener
```java
@KafkaListener(topics = "backtest-request")
public void handleBacktestRequest(BacktestRequest request)
```

### 상태 확인 API (선택사항)
```java
GET /health - 서버 상태 확인
GET /metrics - 처리 중인 백테스트 개수 등
```

## 설정 및 상수

### 거래 설정
```java
// 거래비용
public static final double BUY_FEE_RATE = 0.00015;
public static final double SELL_FEE_RATE = 0.00015;
public static final double TAX_RATE = 0.003;

// 최소 거래 단위
public static final int MIN_TRADING_UNIT = 1;

// 소수점 처리
public static final RoundingMode ROUNDING_MODE = RoundingMode.DOWN;
```

### 리밸런싱 유형
- `THRESHOLD`: 임계값 기반 리밸런싱
- `PERIODIC`: 주기적 리밸런싱

### 리밸런싱 주기
- `MONTHLY`: 월말 기준
- `QUARTERLY`: 분기말 기준

## 개발 가이드

### 프로젝트 실행
```bash
# 개발 환경 실행
./gradlew bootRun

# 테스트 실행
./gradlew test

# 빌드
./gradlew build
```

### 환경 설정
```yaml
# application.yml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: backtest-calculator
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

logging:
  level:
    com.rebra.calculator: DEBUG
```

### 테스트 데이터 생성
```java
// 샘플 백테스트 요청 생성을 위한 테스트 유틸리티
@Component
public class BacktestTestDataBuilder {
    public BacktestRequest createSampleRequest() {
        // 테스트용 데이터 생성 로직
    }
}
```

## 주의사항

1. **데이터 검증**: 받은 OHLCV 데이터의 유효성 검사 필수
2. **에러 처리**: 계산 실패 시 에러 상태로 응답
3. **메모리 관리**: 대용량 데이터 처리 시 메모리 사용량 모니터링
4. **동시성**: 여러 백테스트 요청이 동시에 올 수 있음을 고려

## 확장 계획

1. **캐싱**: Redis를 활용한 계산 결과 캐싱
2. **모니터링**: 처리 시간, 성공/실패율 등 메트릭 수집
3. **배치 처리**: 여러 백테스트를 배치로 처리하는 기능
4. **알림**: 계산 완료 시 알림 기능