# 다중 구독 WebSocket 아키텍처 개선 구현 계획

## 프로젝트 개요

### 목표
현재의 개별 구독 방식에서 다중 구독 방식으로 개선하여, 하나의 WebSocket 세션에서 여러 종목의 다양한 데이터 타입(체결가, 호가, 체결통보 등)을 동시에 구독할 수 있도록 구현합니다.

### 핵심 개념
- **단일 세션 다중 구독**: 하나의 WebSocket 연결에서 여러 종목 × 여러 데이터 타입 동시 구독
- **tr_id 기반 분기**: 수신 데이터를 tr_id(H0STCNT0-체결, H0STASP0-호가)로 구분하여 처리
- **배치 구독 요청**: 클라이언트가 원하는 모든 구독을 배열로 일괄 요청

## 현재 구조 분석

### 1. 연결 관리 방식 (KisApiComponent.java)
```java
// 현재: 계정별 단일 연결
private final Map<String, SubscribableApiResult> connectionPool = new ConcurrentHashMap<>();
private final Map<String, ReentrantLock> connectionLocks = new ConcurrentHashMap<>();

// 연결 키: userId_accountId
private String generateConnectionKey(Long userId, Long accountId) {
    return userId + "_" + accountId;
}
```

**특징:**
- 계정별로 하나의 WebSocket 연결 유지
- 체결가/호가를 위해 각각 별도 API 호출
- 연결 풀링으로 재사용 최적화

### 2. 구독 관리 방식 (WebSocketReconnectionService.java)
```java
// 현재: 타입별 분리 저장
public static class SessionSubscriptions {
    private final Map<String, String> priceSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, String> orderbookSubscriptions = new ConcurrentHashMap<>();
}
```

**특징:**
- 체결가(`priceSubscriptions`)와 호가(`orderbookSubscriptions`) 분리 관리
- 종목별 개별 구독/해제
- 재연결 시 구독 복구 지원

### 3. API 엔드포인트 구조 (RealtimeController.java)
```java
// 현재: 개별 구독 엔드포인트
@MessageMapping("/subscribe/{stockCode}/price")     // 체결가 구독
@MessageMapping("/subscribe/{stockCode}/orderbook") // 호가 구독
@MessageMapping("/unsubscribe/{stockCode}")         // 개별 해제
```

**특징:**
- 종목코드 + 데이터타입별 개별 엔드포인트
- 1:1 매핑 구조 (하나의 요청 → 하나의 구독)
- 순차적 구독 처리

## 제안된 다중 구독 방식

### 1. 클라이언트 구독 요청 예시
```javascript
// 원하는 모든 구독을 배열로 준비
const bulkSubscriptionRequest = {
  stocks: [
    {
      stockCode: "005930", // 삼성전자
      dataTypes: ["price", "orderbook"] // 체결가 + 호가
    },
    {
      stockCode: "000660", // SK하이닉스
      dataTypes: ["price"] // 체결가만
    },
    {
      stockCode: "035720", // 카카오
      dataTypes: ["orderbook"] // 호가만
    }
  ]
};

// 일괄 구독 요청
stompClient.send("/app/subscribe/bulk", {}, JSON.stringify(bulkSubscriptionRequest));
```

### 2. 서버 측 처리 방식
```java
// 수신된 일괄 요청을 각 구독으로 분해하여 순차 처리
for (StockSubscription stock : request.getStocks()) {
    for (String dataType : stock.getDataTypes()) {
        // 기존 개별 구독 로직 재사용
        subscribeToStock(stock.getStockCode(), dataType, sessionId);
    }
}
```

### 3. 데이터 수신 및 분기
```java
// KIS WebSocket에서 수신된 데이터를 tr_id 기반으로 분기
connection.addHandler(data -> {
    if (data instanceof H0STCNT0Data) {
        // 체결가 데이터 처리
        handlePriceData((H0STCNT0Data) data);
    } else if (data instanceof H0STASP0Data) {
        // 호가 데이터 처리
        handleOrderbookData((H0STASP0Data) data);
    }
    // 추가 데이터 타입들...
});
```

## 주요 차이점 및 개선 사항

### 1. 구독 요청 방식
| 구분 | 현재 방식 | 제안된 방식 |
|------|-----------|-------------|
| **요청 수** | 종목×타입 만큼 개별 요청 | 1회 일괄 요청 |
| **네트워크** | 다수의 작은 요청 | 1회의 큰 요청 |
| **에러 처리** | 개별 실패 가능 | 전체 성공/실패 |
| **사용성** | 복잡한 구독 관리 | 단순한 구독 관리 |

### 2. 서버 자원 사용
| 구분 | 현재 방식 | 제안된 방식 |
|------|-----------|-------------|
| **WebSocket 연결** | 계정당 1개 (동일) | 계정당 1개 (동일) |
| **구독 요청 처리** | 순차적 개별 처리 | 배치 처리 |
| **메모리 사용** | 타입별 분리 저장 | 통합 관리 가능 |

### 3. 확장성
| 구분 | 현재 방식 | 제안된 방식 |
|------|-----------|-------------|
| **새 데이터 타입** | 새 엔드포인트 추가 | 기존 구조에 추가 |
| **복잡한 구독** | 클라이언트 복잡도 증가 | 서버에서 처리 |
| **유지보수** | 엔드포인트별 관리 | 중앙 집중 관리 |

## 구현 계획

### Phase 1: 코드 원상복구
**목표**: 현재 주석처리된 체결가 구독 코드를 모두 복구하여 정상 상태로 되돌림

#### 1.1 KisApiComponent.java 복구
- [ ] `startPriceSubscription()` 메서드들 주석 해제
- [ ] `addPriceHandlerToConnection()` 메서드 주석 해제
- [ ] `startRealtimePriceProcessing()` 메서드 주석 해제
- [ ] 연결 생성 로직을 체결가 API(`H0STCNT0Api`)로 복구
- [ ] 호가 구독 시 중복 호가 API 호출 복원

#### 1.2 RealtimeController.java 복구
- [ ] `/subscribe/{stockCode}/price` 엔드포인트 주석 해제
- [ ] 구독 해제에서 price 관련 로직 주석 해제

#### 1.3 KisRealtimeService.java 복구
- [ ] `startPriceSubscription()` 메서드 주석 해제
- [ ] `stopPriceSubscription()` 메서드 주석 해제

#### 1.4 테스트
- [ ] 체결가 구독 정상 동작 확인
- [ ] 호가 구독 정상 동작 확인
- [ ] 동시 구독 시 데이터 수신 확인

### Phase 2: 다중 구독 DTO 클래스 생성
**목표**: 일괄 구독 요청/응답을 위한 데이터 클래스들 구현

#### 2.1 요청 DTO 생성
```java
// src/main/java/com/rebra/dto/request/BulkSubscriptionRequest.java
public class BulkSubscriptionRequest {
    private List<StockSubscription> stocks;
    // getter, setter, validation
}

// src/main/java/com/rebra/dto/request/StockSubscription.java
public class StockSubscription {
    @NotBlank
    private String stockCode;

    @NotEmpty
    private List<String> dataTypes; // ["price", "orderbook", ...]

    // getter, setter, validation
}

// src/main/java/com/rebra/dto/request/BulkUnsubscriptionRequest.java
public class BulkUnsubscriptionRequest {
    private List<StockUnsubscription> stocks;
    // 구독 해제용 DTO
}
```

#### 2.2 응답 DTO 생성
```java
// src/main/java/com/rebra/dto/response/BulkSubscriptionResponse.java
public class BulkSubscriptionResponse {
    private boolean success;
    private List<SubscriptionResult> results;
    private String message;
    // 일괄 구독 결과
}

// src/main/java/com/rebra/dto/response/SubscriptionResult.java
public class SubscriptionResult {
    private String stockCode;
    private String dataType;
    private boolean success;
    private String errorMessage;
    // 개별 구독 결과
}
```

### Phase 3: Bulk API 엔드포인트 구현
**목표**: 일괄 구독/해제를 처리하는 새로운 WebSocket 엔드포인트 추가

#### 3.1 RealtimeController 확장
```java
/**
 * 일괄 구독 요청
 * 클라이언트: SEND("/app/subscribe/bulk", {stocks: [...]})
 */
@MessageMapping("/subscribe/bulk")
public void subscribeBulkRequest(@RequestBody BulkSubscriptionRequest request,
                                SimpMessageHeaderAccessor headerAccessor) {
    // 구현 내용
}

/**
 * 일괄 구독 해제 요청
 * 클라이언트: SEND("/app/unsubscribe/bulk", {stocks: [...]})
 */
@MessageMapping("/unsubscribe/bulk")
public void unsubscribeBulkRequest(@RequestBody BulkUnsubscriptionRequest request,
                                  SimpMessageHeaderAccessor headerAccessor) {
    // 구현 내용
}
```

#### 3.2 구현 세부사항
- [ ] 입력 검증 (종목코드 형식, 데이터타입 유효성)
- [ ] 권한 확인 (사용자 인증, 계좌 접근 권한)
- [ ] 트랜잭션 처리 (일부 실패 시 롤백 정책)
- [ ] 에러 응답 (개별 실패 원인 상세 제공)
- [ ] 성능 최적화 (병렬 처리, 배치 처리)

### Phase 4: KisApiComponent 다중 구독 지원
**목표**: 단일 연결에서 여러 tr_id를 동시에 구독할 수 있도록 개선

#### 4.1 새로운 메서드 추가
```java
/**
 * 일괄 구독 시작
 */
public BulkSubscriptionResponse startBulkSubscription(
    Account account,
    List<StockSubscription> stockSubscriptions,
    String sessionId) {
    // 구현 내용
}

/**
 * 연결별 다중 데이터 핸들러 관리
 */
private void addMultipleHandlersToConnection(
    SubscribableApiResult connection,
    Map<String, Set<String>> stockDataTypes,
    Map<String, Consumer<Object>> handlers) {
    // 구현 내용
}
```

#### 4.2 기존 메서드 개선
- [ ] `getOrCreateConnection()` - 다중 API 동시 구독 지원
- [ ] `addOrderbookHandlerToConnection()` - 여러 종목 동시 처리
- [ ] `addPriceHandlerToConnection()` - 여러 종목 동시 처리
- [ ] 에러 처리 강화 (부분 실패 시 재시도 로직)

### Phase 5: WebSocketReconnectionService 개선
**목표**: 통합된 구독 관리와 기존 방식의 호환성 유지

#### 5.1 SessionSubscriptions 구조 개선
```java
public static class SessionSubscriptions {
    // 기존 방식 (하위 호환성)
    private final Map<String, String> priceSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, String> orderbookSubscriptions = new ConcurrentHashMap<>();

    // 새로운 통합 방식
    private final Map<String, Set<String>> stockSubscriptions = new ConcurrentHashMap<>();
    // 종목코드 -> 데이터타입 집합 (예: "005930" -> {"price", "orderbook"})

    private Long userId;
    private int reconnectionAttempts = 0;
}
```

#### 5.2 구독 관리 메서드 개선
- [ ] `addBulkSubscription()` - 일괄 구독 정보 추가
- [ ] `removeBulkSubscription()` - 일괄 구독 정보 제거
- [ ] `recoverBulkSubscriptions()` - 일괄 구독 복구
- [ ] 기존 개별 메서드들과 호환성 유지

### Phase 6: 테스트 및 검증
**목표**: 모든 기능의 정상 동작과 성능 확인

#### 6.1 단위 테스트
- [ ] DTO 클래스 직렬화/역직렬화 테스트
- [ ] KisApiComponent 다중 구독 로직 테스트
- [ ] WebSocketReconnectionService 통합 관리 테스트

#### 6.2 통합 테스트
- [ ] 기존 개별 구독 API 정상 동작 확인
- [ ] 새로운 bulk API 기능 테스트
- [ ] 혼합 사용 시나리오 (개별 + bulk) 검증
- [ ] 대량 구독 시 성능 테스트
- [ ] 연결 장애 시 복구 테스트

#### 6.3 성능 테스트
- [ ] 동시 사용자 100명 × 10종목 × 2데이터타입 = 2000구독
- [ ] 메모리 사용량 측정
- [ ] CPU 사용률 측정
- [ ] 네트워크 대역폭 사용량 측정

## 예상되는 이슈 및 해결 방안

### 1. 기술적 이슈

#### Issue: KIS API 라이브러리 제약
**문제**: KIS WebSocket이 동시 다중 구독을 지원하는지 불명확
**해결**:
- 단계적 구독 요청 (순차적으로 여러 구독 등록)
- 연결당 최대 구독 수 제한 설정
- 필요시 다중 연결 사용 (fallback)

#### Issue: 메모리 사용량 증가
**문제**: 다중 구독으로 인한 메모리 사용량 급증 가능
**해결**:
- 구독 수 제한 (사용자당 최대 N개)
- LRU 캐시로 비활성 구독 자동 해제
- 메모리 모니터링 및 알림

#### Issue: 데이터 처리 성능
**문제**: 대량의 실시간 데이터 수신 시 처리 지연
**해결**:
- 비동기 처리 (CompletableFuture, @Async)
- 데이터 큐잉 및 배치 처리
- 백프레셔(backpressure) 제어

### 2. 운영상 이슈

#### Issue: 구독 복잡도 증가
**문제**: 개발자가 관리해야 할 구독 상태 복잡화
**해결**:
- 구독 상태 모니터링 대시보드
- 로깅 강화 (구독/해제 이력 추적)
- 디버깅 도구 제공

#### Issue: 에러 디버깅 어려움
**문제**: 일괄 구독에서 부분 실패 시 원인 파악 곤란
**해결**:
- 상세한 에러 응답 (종목별 실패 원인)
- 구조화된 로깅 (JSON 형태)
- 알림 및 모니터링 연동

### 3. 사용자 경험 이슈

#### Issue: 구독 설정 복잡성
**문제**: 사용자가 원하는 구독을 설정하기 어려움
**해결**:
- 프리셋 제공 (인기 종목 패키지, 테마별 패키지)
- UI/UX 개선 (드래그앤드롭, 템플릿)
- 추천 시스템 (사용 패턴 기반)

## 성공 지표

### 1. 기능적 지표
- [ ] 기존 개별 구독 API 100% 호환성 유지
- [ ] 일괄 구독 API 정상 동작 (성공률 > 99%)
- [ ] 구독 복구 정상 동작 (장애 시 자동 복구율 > 95%)

### 2. 성능 지표
- [ ] 구독 처리 시간: 개별 < 500ms, 일괄(10개) < 2s
- [ ] 메모리 사용량: 기존 대비 < 150% 증가
- [ ] CPU 사용률: 기존 대비 < 120% 증가

### 3. 사용성 지표
- [ ] 개발자 만족도: 5점 만점에 4점 이상
- [ ] 구독 설정 시간: 기존 대비 50% 단축
- [ ] 에러 발생률: 전체 구독 대비 < 1%

## 향후 확장 계획

### 1. 추가 데이터 타입 지원
- 체결통보 (주문 체결 알림)
- 뉴스 피드 (종목별 실시간 뉴스)
- 공시 정보 (실시간 공시)
- 지수 데이터 (코스피, 코스닥 등)

### 2. 고급 구독 기능
- 조건부 구독 (가격 조건, 거래량 조건)
- 스마트 구독 (AI 기반 자동 구독/해제)
- 그룹 구독 (포트폴리오별, 테마별)

### 3. 성능 최적화
- 데이터 압축 (gzip, 델타 압축)
- 캐싱 전략 (Redis 클러스터)
- CDN 활용 (정적 데이터 분리)

---

## 결론

이 구현 계획을 통해 현재의 개별 구독 방식을 점진적으로 다중 구독 방식으로 개선할 수 있습니다. 기존 시스템과의 호환성을 유지하면서도 새로운 기능을 추가하여, 사용자에게는 더 편리한 구독 경험을, 개발자에게는 더 유지보수하기 쉬운 코드를 제공할 수 있습니다.

핵심은 **점진적 개선**과 **하위 호환성 유지**입니다. 기존 개별 구독 API를 그대로 유지하면서 새로운 bulk API를 추가하여, 사용자가 필요에 따라 선택할 수 있도록 하는 것이 성공의 열쇠입니다.