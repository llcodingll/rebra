package com.rebra.component;

import com.rebra.component.kisApi.FluctuationRanking;
import com.rebra.component.kisApi.FluctuationRankingResult;
import com.rebra.component.kisApi.VolumeRank;
import com.rebra.component.kisApi.VolumeRankResult;
import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.entity.Account;
import com.rebra.entity.AccountType;
import com.rebra.exception.kis.KisException;
import com.rebra.util.AccountEncryptionUtil;
import com.youhogeon.finance.kis_api.KisClient;
import com.youhogeon.finance.kis_api.api.realtime.H0STASP0Api;
import com.youhogeon.finance.kis_api.api.realtime.H0STASP0Data;
import com.youhogeon.finance.kis_api.api.realtime.H0STCNT0Api;
import com.youhogeon.finance.kis_api.api.realtime.H0STCNT0Data;
import com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceApi;
import com.youhogeon.finance.kis_api.api.rest.quotations.InquireDailyItemchartpriceResult;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceApi;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import com.youhogeon.finance.kis_api.client.socket.SubscribableApiResult;
import com.youhogeon.finance.kis_api.config.Configuration;
import com.youhogeon.finance.kis_api.config.Credentials;
import com.youhogeon.finance.kis_api.exception.KisClientException;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * KIS API 클라이언트 및 연결 관리 컴포넌트
 */
@Component
public class KisApiComponent {

    private static final Logger log = LoggerFactory.getLogger(KisApiComponent.class);

    private Configuration mockConfig;  // 모의투자용 Configuration
    private Configuration realConfig;  // 실계좌용 Configuration
    private KisClient mockClient;      // 모의투자용 Client
    private KisClient realClient;      // 실계좌용 Client

    // 사용자별 Credentials 명 관리
    private final Map<String, String> userCredentialsMap = new ConcurrentHashMap<>();

    // 실시간 구독 관리
    private final Map<String, AtomicInteger> subscriptionCount = new ConcurrentHashMap<>();
    private final Map<String, SubscribableApiResult> activeSubscriptions = new ConcurrentHashMap<>();

    // WebSocket 연결 풀링 (AppKey별 단일 연결 관리)
    private final Map<String, SubscribableApiResult> connectionPool = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> connectionLocks = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeConfigurations() {
        log.info("KIS API Configuration 초기화 시작");

        // 모의투자용 Configuration 생성
        mockConfig = new Configuration();
        mockConfig.setHttpHost("https://openapivts.koreainvestment.com:29443");
        mockConfig.setHttpTimeout(Duration.ofSeconds(30));
        mockConfig.setHttpTimeoutMaxRetries(3);
        mockConfig.setSocketHost("ws://ops.koreainvestment.com:31000");

        // WebSocket 버퍼 크기 설정 (메시지 크기 초과 문제 해결)
        // KIS Configuration에서 직접 설정할 수 없으므로 시스템 속성으로 처리

        // 실계좌용 Configuration 생성
        realConfig = new Configuration();
        realConfig.setHttpHost("https://openapi.koreainvestment.com:9443");
        realConfig.setHttpTimeout(Duration.ofSeconds(30));
        realConfig.setHttpTimeoutMaxRetries(3);
        realConfig.setSocketHost("ws://ops.koreainvestment.com:21000");

        // WebSocket 버퍼 크기 설정 (메시지 크기 초과 문제 해결)
        // KIS Configuration에서 직접 설정할 수 없으므로 시스템 속성으로 처리

        // KisClient 생성
        mockClient = new KisClient(mockConfig);
        realClient = new KisClient(realConfig);

        log.info("KIS API Configuration 초기화 완료");
        log.info("📡 WebSocket 설정 - 최대 메시지 크기: {}MB, 세션 타임아웃: {}분",
            1, 30);
    }

    /**
     * 사용자 Credentials 등록 (신규 사용자 등록)
     */
    public void addUserCredentials(Long userId, Long accountId, String accountNumber,
                                   String appKey, String appSecret, AccountType accountType) {
        try {
            String credentialsName = generateCredentialsName(userId, accountId);

            // 계좌번호를 앞 8자리, 뒤 2자리로 분리
            String accountPre = accountNumber.length() >= 8 ? accountNumber.substring(0, 8) : accountNumber;
            String accountPost = accountNumber.length() > 8 ? accountNumber.substring(8) : "01";

            Credentials credentials = new Credentials(appKey, appSecret, accountPre, accountPost);

            // 매핑 정보 저장
            userCredentialsMap.put(userId + "_" + accountId, credentialsName);

            // 프론트에서 받은 계좌 타입에 따라 적절한 Configuration에 추가
            if (accountType == AccountType.MOCK) {
                credentials.setRestLimitPerSecond(1);
                mockConfig.addCredentials(credentialsName, credentials);
                mockClient = new KisClient(mockConfig);
            } else {
                realConfig.addCredentials(credentialsName, credentials);
                realClient = new KisClient(realConfig);
            }

            log.info("사용자 Credentials 등록 완료 - 사용자ID: {}, 계좌ID: {}, 타입: {}, 이름: {}",
                    userId, accountId, accountType, credentialsName);

        } catch (Exception e) {
            log.error("사용자 Credentials 등록 실패 - 사용자ID: {}, 계좌ID: {}", userId, accountId, e);
            throw new RuntimeException("사용자 Credentials 등록 실패", e);
        }
    }

    /**
     * DB O / Config X => 이 경우 엔티티를 통해서 Credentials를 Config에 바로 저장합니다. Config는 항상 초기화 하면 안됨 -> 액세스 토큰이 들어있음 항상
     * Credentials 등록 시엔 verifyAccount로 해당 앱키가 유효한지 확인해야함!
     */
    public void ensureUserCredentials(Long userId, Long accountId, AccountType accountType,
                                      DecryptedAccountCredentials credentials) {
        if (userCredentialsMap.containsKey(generateCredentialsName(userId, accountId))) {
            return;
        }

        addUserCredentials(userId, accountId, credentials.getAccountNumber(), credentials.getAppKey(),
                credentials.getAppSecret(),
                accountType);
    }


    /**
     * 사용자 Credentials 제거
     */
    public void removeUserCredentials(Long userId, Long accountId, AccountType accountType) {
        String userKey = generateCredentialsName(userId, accountId);
        String credentialsName = userCredentialsMap.get(userKey);

        if (credentialsName == null) {
            return;
        }

        if (accountType == AccountType.MOCK) {
            mockConfig.removeCredentials(credentialsName);
            mockClient = new KisClient(mockConfig);
        } else {
            realConfig.removeCredentials(credentialsName);
            realClient = new KisClient(realConfig);
        }

        userCredentialsMap.remove(userKey);
    }

    /**
     * Credentials 명 생성
     */
    private String generateCredentialsName(Long userId, Long accountId) {
        return userId + "_" + accountId;
    }

    /**
     * 사용자의 Credentials 명 조회
     */
    private String getUserCredentialsName(Long userId, Long accountId) {
        String userKey = userId + "_" + accountId;
        return userCredentialsMap.get(userKey);
    }

    /**
     * KIS API 연결 테스트 및 계좌 타입 반환 (단일 사용 - 계좌 등록용)
     */
    public void verifyAccount(String accountNumber, String appKey, String appSecret, AccountType accountType) {
        try {
            log.info("KIS API 연결 테스트 시작 - 계좌번호: {}, 계좌타입: {}", accountNumber, accountType);

            // 계좌번호를 앞 8자리, 뒤 2자리로 분리 (실무 표준)
            String accountPre = accountNumber.length() >= 8 ? accountNumber.substring(0, 8) : accountNumber;
            String accountPost = accountNumber.length() > 8 ? accountNumber.substring(8) : "01"; // 일반계좌 기본값

            Credentials credentials = new Credentials(appKey, appSecret, accountPre, accountPost);

            Configuration config = new Configuration();
            config.addCredentials(credentials);

            // 계좌 타입에 따른 HTTP 호스트 설정
            if (accountType == AccountType.MOCK) {
                config.setHttpHost("https://openapivts.koreainvestment.com:29443");
            }

            KisClient client = new KisClient(config);

            // 잔고조회 API로 연결 테스트
            InquireBalanceApi req = new InquireBalanceApi();

            // 계좌 타입에 따른 TR ID 설정
            if (accountType == AccountType.MOCK) {
                req.setTrId("VTTC8434R");  // 모의투자 잔고조회
            }

            InquireBalanceResult result = client.execute(req);

            if (!result.getRtCd().equals("0")) {
                throw new RuntimeException("KIS API 인증 실패");
            }

        } catch (Exception e) {
            log.error("KIS API 연결 테스트 중 예외 발생 - 계좌번호: {}, 계좌타입: {}, 오류: {}",
                    accountNumber, accountType, e.getMessage(), e);
            throw new RuntimeException("KIS API 연결 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 계좌 재연결 로직
     *
     * @param accountCredentials
     * @param accountType
     */
    public void verifyAccount(DecryptedAccountCredentials accountCredentials, AccountType accountType) {
        verifyAccount(accountCredentials.getAccountNumber(), accountCredentials.getAppKey(),
                accountCredentials.getAppSecret(), accountType);
    }


    /**
     * 사용자 잔고 조회 (Account 객체 사용)
     */
    public InquireBalanceResult getUserBalance(Account account) {
        Long userId = account.getUser().getId();
        DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
        return getUserBalance(userId, account.getId(), account.getAccountType(), credentials);
    }

    /**
     * 등록된 사용자 Credentials로 잔고 조회 ensureUserCredentials()를 통해 Credentials가 없으면 자동으로 등록
     */
    public InquireBalanceResult getUserBalance(Long userId, Long accountId, AccountType accountType,
                                               DecryptedAccountCredentials credentials) {
        try {
            log.info("사용자 잔고 조회 시작 - 사용자ID: {}, 계좌ID: {}, 계좌타입: {}", userId, accountId, accountType);

            // Credentials가 Config에 없으면 자동으로 등록
            ensureUserCredentials(userId, accountId, accountType, credentials);

            String credentialsName = getUserCredentialsName(userId, accountId);
            if (credentialsName == null) {
                log.error("ensureUserCredentials 후에도 Credentials를 찾을 수 없음 - 사용자ID: {}, 계좌ID: {}", userId, accountId);
                throw new RuntimeException("Credentials 등록 실패");
            }

            KisClient client = accountType == AccountType.MOCK ? mockClient : realClient;

            InquireBalanceApi req = new InquireBalanceApi();

            if (accountType == AccountType.MOCK) {
                req.setTrId("VTTC8434R");
            }

            InquireBalanceResult result = client.execute(req, credentialsName);

            // rtCd가 "0"이 아니면 실패 (KIS API 표준)
            if (!result.getRtCd().equals("0")) {
                throw new RuntimeException("KIS API 잔고조회 실패");
            }

            return result;
        } catch (Exception e) {
            log.error("사용자 잔고 조회 실패 - 사용자ID: {}, 계좌ID: {}, 오류: {}",
                    userId, accountId, e.getMessage(), e);
            throw new RuntimeException("사용자 잔고 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 실시간 체결가 구독 시작 (Account 객체 사용)
     */
    public void startPriceSubscription(Account account, String stockCode, Consumer<H0STCNT0Data> dataHandler) {
        Long userId = account.getUser().getId();
        DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
        startPriceSubscription(userId, account.getId(), stockCode, account.getAccountType(), credentials, dataHandler);
    }

    /**
     * 실시간 체결가 구독 시작 (공유 연결 사용)
     */
    public void startPriceSubscription(Long userId, Long accountId, String stockCode,
                                       AccountType accountType, DecryptedAccountCredentials credentials,
                                       Consumer<H0STCNT0Data> dataHandler) {
        String subscriptionKey = generateSubscriptionKey(userId, stockCode, "price");

        ensureUserCredentials(userId, accountId, accountType, credentials);
        try {
            // 체결가 전용 WebSocket 연결 획득
            SubscribableApiResult priceConnection = getOrCreatePriceConnection(userId, accountId, accountType, stockCode, credentials);

            // 구독 참조 카운트 증가
            int count = subscriptionCount.computeIfAbsent(subscriptionKey, k -> new AtomicInteger(0)).incrementAndGet();

            log.info("📈 실시간 체결가 구독 시작 - UserId: {}, StockCode: {}, 구독자: {}명 (체결가 전용 연결)",
                    userId, stockCode, count);

            // 체결가 연결에 체결가 데이터 핸들러 추가
            addPriceHandlerToConnection(priceConnection, stockCode, dataHandler);

            activeSubscriptions.put(subscriptionKey, priceConnection);

            log.info("✅ 실시간 체결가 구독 완료 - StockCode: {}", stockCode);

        } catch (Exception e) {
            // 실패 시 구독 카운트 원복
            subscriptionCount.computeIfPresent(subscriptionKey, (k, v) -> {
                int newCount = v.decrementAndGet();
                if (newCount <= 0) {
                    subscriptionCount.remove(k);
                }
                return newCount > 0 ? v : null;
            });

            log.error("❌ 실시간 체결가 구독 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
            throw new RuntimeException("실시간 체결가 구독 실패", e);
        }
    }

    /**
     * 실시간 호가 구독 시작 (Account 객체 사용)
     */
    public void startOrderbookSubscription(Account account, String stockCode, Consumer<H0STASP0Data> dataHandler) {
        Long userId = account.getUser().getId();
        DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
        startOrderbookSubscription(userId, account.getId(), stockCode, account.getAccountType(), credentials, dataHandler);
    }

    /**
     * 실시간 호가 구독 시작 (공유 연결 사용)
     */
    public void startOrderbookSubscription(Long userId, Long accountId, String stockCode,
                                           AccountType accountType, DecryptedAccountCredentials credentials,
                                           Consumer<H0STASP0Data> dataHandler) {
        String subscriptionKey = generateSubscriptionKey(userId, stockCode, "orderbook");

        ensureUserCredentials(userId, accountId, accountType, credentials);
        try {
            // 호가 전용 WebSocket 연결 획득
            SubscribableApiResult orderbookConnection = getOrCreateOrderbookConnection(userId, accountId, accountType, stockCode, credentials);

            // 구독 참조 카운트 증가
            int count = subscriptionCount.computeIfAbsent(subscriptionKey, k -> new AtomicInteger(0)).incrementAndGet();

            log.info("📊 실시간 호가 구독 시작 - UserId: {}, StockCode: {}, 구독자: {}명 (호가 전용 연결)",
                    userId, stockCode, count);

            // 호가 연결에 호가 데이터 핸들러 추가
            addOrderbookHandlerToConnection(orderbookConnection, stockCode, dataHandler);

            activeSubscriptions.put(subscriptionKey, orderbookConnection);

            log.info("✅ 실시간 호가 구독 완료 - StockCode: {}", stockCode);

        } catch (Exception e) {
            // 실패 시 구독 카운트 원복
            subscriptionCount.computeIfPresent(subscriptionKey, (k, v) -> {
                int newCount = v.decrementAndGet();
                if (newCount <= 0) {
                    subscriptionCount.remove(k);
                }
                return newCount > 0 ? v : null;
            });

            log.error("❌ 실시간 호가 구독 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
            throw new RuntimeException("실시간 호가 구독 실패", e);
        }
    }

    /**
     * 실시간 구독 해제
     */
    public void stopSubscription(Long userId, String stockCode, String dataType) {
        try {
            String subscriptionKey = generateSubscriptionKey(userId, stockCode, dataType);

            AtomicInteger counter = subscriptionCount.get(subscriptionKey);
            if (counter == null) {
                log.warn("구독하지 않은 데이터 타입 해제 시도 - UserId: {}, StockCode: {}, Type: {}",
                        userId, stockCode, dataType);
                return;
            }

            int count = counter.decrementAndGet();

            // 마지막 구독자가 해제하는 경우 실제 WebSocket 연결 종료
            if (count == 0) {
                SubscribableApiResult subscription = activeSubscriptions.remove(subscriptionKey);
                if (subscription != null) {
                    subscription.unsubscribe();
                    log.info("실시간 {} 구독 완전 해제 - UserId: {}, StockCode: {}",
                            dataType, userId, stockCode);
                }
                subscriptionCount.remove(subscriptionKey);
            } else {
                log.info("실시간 {} 구독자 감소 - UserId: {}, StockCode: {}, 남은 구독자: {}명",
                        dataType, userId, stockCode, count);
            }

        } catch (Exception e) {
            log.error("실시간 구독 해제 실패 - UserId: {}, StockCode: {}, Type: {}",
                    userId, stockCode, dataType, e);
        }
    }

    /**
     * 구독 키 생성 (사용자별 구독 관리)
     */
    private String generateSubscriptionKey(Long userId, String stockCode, String dataType) {
        return userId + ":" + stockCode + ":" + dataType;
    }

    /**
     * 연결 키 생성 (AppKey별 연결 관리)
     */
    private String generateConnectionKey(Long userId, Long accountId) {
        return userId + "_" + accountId;
    }

    /**
     * 체결가용 WebSocket 연결 획득 또는 생성
     */
    private SubscribableApiResult getOrCreatePriceConnection(Long userId, Long accountId, AccountType accountType,
                                                            String stockCode, DecryptedAccountCredentials credentials) {
        String connectionKey = generateConnectionKey(userId, accountId) + "_PRICE";

        // Credentials가 Config에 없으면 자동으로 등록
        ensureUserCredentials(userId, accountId, accountType, credentials);

        String credentialsName = getUserCredentialsName(userId, accountId);
        if (credentialsName == null) {
            log.error("ensureUserCredentials 후에도 Credentials를 찾을 수 없음 - 사용자ID: {}, 계좌ID: {}", userId, accountId);
            throw new RuntimeException("Credentials 등록 실패");
        }

        // 연결별 락 획득
        ReentrantLock connectionLock = connectionLocks.computeIfAbsent(connectionKey, k -> new ReentrantLock());

        connectionLock.lock();
        try {
            // 기존 연결이 있는지 확인
//            SubscribableApiResult existingConnection = connectionPool.get(connectionKey);
////            if (existingConnection != null) {
////                log.info("🔗 기존 체결가 WebSocket 연결 재사용 - ConnectionKey: {}, StockCode: {}", connectionKey, stockCode);
////                return existingConnection;
////            }

            // 새 체결가 연결 생성
            log.info("🆕 새 체결가 WebSocket 연결 생성 - ConnectionKey: {}, StockCode: {}", connectionKey, stockCode);
            KisClient client = accountType == AccountType.MOCK ? mockClient : realClient;

            // 체결가 API로 연결 생성
            H0STCNT0Api priceApi = new H0STCNT0Api(stockCode);

            SubscribableApiResult newConnection = executeWithRetry(() -> {
                log.info("🔄 체결가 WebSocket 연결 생성 시도 - ConnectionKey: {}, StockCode: {}", connectionKey, stockCode);
                return client.execute(priceApi, credentialsName);
            }, 3, "체결가 WebSocket 연결 생성");

            // 연결 풀에 저장
            connectionPool.put(connectionKey, newConnection);
            log.info("✅ 체결가 WebSocket 연결 생성 완료 - ConnectionKey: {}, StockCode: {}", connectionKey, stockCode);

            return newConnection;

        } finally {
            connectionLock.unlock();
        }
    }

    /**
     * 호가용 WebSocket 연결 획득 또는 생성
     */
    private SubscribableApiResult getOrCreateOrderbookConnection(Long userId, Long accountId, AccountType accountType,
                                                                String stockCode, DecryptedAccountCredentials credentials) {
        String connectionKey = generateConnectionKey(userId, accountId) + "_ORDERBOOK";

        // Credentials가 Config에 없으면 자동으로 등록
        ensureUserCredentials(userId, accountId, accountType, credentials);

        String credentialsName = getUserCredentialsName(userId, accountId);
        if (credentialsName == null) {
            log.error("ensureUserCredentials 후에도 Credentials를 찾을 수 없음 - 사용자ID: {}, 계좌ID: {}", userId, accountId);
            throw new RuntimeException("Credentials 등록 실패");
        }

        // 연결별 락 획득
        ReentrantLock connectionLock = connectionLocks.computeIfAbsent(connectionKey, k -> new ReentrantLock());

        connectionLock.lock();
        try {
            // 기존 연결이 있는지 확인
//            SubscribableApiResult existingConnection = connectionPool.get(connectionKey);
//            if (existingConnection != null) {
//                log.info("🔗 기존 호가 WebSocket 연결 재사용 - ConnectionKey: {}, StockCode: {}", connectionKey, stockCode);
//                return existingConnection;
//            }

            // 새 호가 연결 생성
            log.info("🆕 새 호가 WebSocket 연결 생성 - ConnectionKey: {}, StockCode: {}", connectionKey, stockCode);
            KisClient client = accountType == AccountType.MOCK ? mockClient : realClient;

            // 호가 API로 연결 생성
            H0STASP0Api orderbookApi = new H0STASP0Api(stockCode);

            SubscribableApiResult newConnection = executeWithRetry(() -> {
                log.info("🔄 호가 WebSocket 연결 생성 시도 - ConnectionKey: {}, StockCode: {}", connectionKey, stockCode);
                return client.execute(orderbookApi, credentialsName);
            }, 3, "호가 WebSocket 연결 생성");

            // 연결 풀에 저장
            connectionPool.put(connectionKey, newConnection);
            log.info("✅ 호가 WebSocket 연결 생성 완료 - ConnectionKey: {}, StockCode: {}", connectionKey, stockCode);

            return newConnection;

        } finally {
            connectionLock.unlock();
        }
    }

    /**
     * WebSocket 연결 해제
     */
    private void closePriceConnection(Long userId, Long accountId) {
        String connectionKey = generateConnectionKey(userId, accountId) + "_PRICE";
        ReentrantLock connectionLock = connectionLocks.get(connectionKey);

        if (connectionLock != null) {
            connectionLock.lock();
            try {
                SubscribableApiResult connection = connectionPool.remove(connectionKey);
                if (connection != null) {
                    connection.unsubscribe();
                    log.info("🔌 체결가 WebSocket 연결 해제 - ConnectionKey: {}", connectionKey);
                }
            } finally {
                connectionLock.unlock();
                connectionLocks.remove(connectionKey);
            }
        }
    }

    private void closeOrderbookConnection(Long userId, Long accountId) {
        String connectionKey = generateConnectionKey(userId, accountId) + "_ORDERBOOK";
        ReentrantLock connectionLock = connectionLocks.get(connectionKey);

        if (connectionLock != null) {
            connectionLock.lock();
            try {
                SubscribableApiResult connection = connectionPool.remove(connectionKey);
                if (connection != null) {
                    connection.unsubscribe();
                    log.info("🔌 호가 WebSocket 연결 해제 - ConnectionKey: {}", connectionKey);
                }
            } finally {
                connectionLock.unlock();
                connectionLocks.remove(connectionKey);
            }
        }
    }

    /**
     * 공유 연결에 체결가 데이터 핸들러 추가
     */
    private void addPriceHandlerToConnection(SubscribableApiResult connection, String stockCode,
                                           Consumer<H0STCNT0Data> dataHandler) {
        try {
            log.info("🔗 공유 연결에 체결가 핸들러 추가 - StockCode: {}", stockCode);

            // 기존 핸들러에 새로운 데이터 핸들러 추가
            connection.addHandler(data -> {
                try {
                    // 모든 수신 데이터 로깅 (디버깅용)
                    log.info("📡 WebSocket 데이터 수신 - Type: {}, Data: {}",
                        data != null ? data.getClass().getSimpleName() : "null", data);

                    if (data instanceof H0STCNT0Data[]) {
                        H0STCNT0Data[] priceDataArray = (H0STCNT0Data[]) data;
                        log.info("📊 체결가 배열 데이터 수신 - StockCode요청: {}, 배열크기: {}", stockCode, priceDataArray.length);

                        for (H0STCNT0Data priceData : priceDataArray) {
                            // 종목코드 필드들 모두 로깅
                            log.info("💰 체결가 데이터 상세 - StockCode요청: {}, MkscShrnIscd: {}, StckShrnIscd: {}, Price: {}",
                                    stockCode, priceData.getMkscShrnIscd(),
                                    getFieldSafely(() -> priceData.getMkscShrnIscd(), "N/A"),
                                    priceData.getStckPrpr());

                            // 종목코드 필터링 (해당 종목만 처리)
                            if (stockCode.equals(priceData.getMkscShrnIscd()) ||
                                stockCode.equals(getFieldSafely(() -> priceData.getMkscShrnIscd(), ""))) {

                                log.info("✅ 체결가 데이터 매칭 - StockCode: {}, Price: {}",
                                        stockCode, priceData.getStckPrpr());

                                // 데이터 핸들러를 통해 KisRealtimeService로 데이터 전달
                                dataHandler.accept(priceData);
                            } else {
                                log.debug("⏭️ 체결가 데이터 스킵 - 요청: {}, 수신: {}",
                                        stockCode, priceData.getMkscShrnIscd());
                            }
                        }
                    } else if (data instanceof H0STCNT0Data) {
                        // 단일 데이터 처리 (하위 호환성)
                        H0STCNT0Data priceData = (H0STCNT0Data) data;

                        // 종목코드 필드들 모두 로깅
                        log.info("💰 체결가 단일 데이터 상세 - StockCode요청: {}, MkscShrnIscd: {}, StckShrnIscd: {}, Price: {}",
                                stockCode, priceData.getMkscShrnIscd(),
                                getFieldSafely(() -> priceData.getMkscShrnIscd(), "N/A"),
                                priceData.getStckPrpr());

                        // 종목코드 필터링 (해당 종목만 처리)
                        if (stockCode.equals(priceData.getMkscShrnIscd()) ||
                            stockCode.equals(getFieldSafely(() -> priceData.getMkscShrnIscd(), ""))) {

                            log.info("✅ 체결가 데이터 매칭 - StockCode: {}, Price: {}",
                                    stockCode, priceData.getStckPrpr());

                            // 데이터 핸들러를 통해 KisRealtimeService로 데이터 전달
                            dataHandler.accept(priceData);
                        } else {
                            log.debug("⏭️ 체결가 데이터 스킵 - 요청: {}, 수신: {}",
                                    stockCode, priceData.getMkscShrnIscd());
                        }
                    } else {
                        log.debug("🔍 체결가 핸들러: 다른 타입 데이터 - Type: {}",
                            data != null ? data.getClass().getSimpleName() : "null");
                    }
                } catch (Exception e) {
                    log.error("❌ 실시간 체결가 데이터 처리 중 오류 - StockCode: {}", stockCode, e);
                }
            });

            log.info("✅ 체결가 핸들러 추가 완료 - StockCode: {}", stockCode);

        } catch (Exception e) {
            log.error("❌ 체결가 핸들러 추가 실패 - StockCode: {}", stockCode, e);
            throw new RuntimeException("체결가 핸들러 추가 실패", e);
        }
    }

    /**
     * 공유 연결에 호가 데이터 핸들러 추가
     */
    private void addOrderbookHandlerToConnection(SubscribableApiResult connection, String stockCode,
                                               Consumer<H0STASP0Data> dataHandler) {
        try {
            log.info("🔗 공유 연결에 호가 핸들러 추가 - StockCode: {}", stockCode);

            // 기존 핸들러에 새로운 데이터 핸들러 추가
            connection.addHandler(data -> {
                try {
                    // 모든 수신 데이터 로깅 (디버깅용)
                    log.info("📡 WebSocket 데이터 수신 - Type: {}, Data: {}",
                        data != null ? data.getClass().getSimpleName() : "null", data);

                    if (data instanceof H0STASP0Data[]) {
                        H0STASP0Data[] orderbookDataArray = (H0STASP0Data[]) data;
                        log.info("📊 호가 배열 데이터 수신 - StockCode요청: {}, 배열크기: {}", stockCode, orderbookDataArray.length);

                        for (H0STASP0Data orderbookData : orderbookDataArray) {
                            // 종목코드 필드들 모두 로깅
                            log.info("📊 호가 데이터 상세 - StockCode요청: {}, MkscShrnIscd: {}, StckShrnIscd: {}, AskPrice1: {}, BidPrice1: {}",
                                    stockCode, orderbookData.getMkscShrnIscd(),
                                    getFieldSafely(() -> orderbookData.getMkscShrnIscd(), "N/A"),
                                    orderbookData.getAskp1(), orderbookData.getBidp1());

                            // 종목코드 필터링 (해당 종목만 처리)
                            if (stockCode.equals(orderbookData.getMkscShrnIscd()) ||
                                stockCode.equals(getFieldSafely(() -> orderbookData.getMkscShrnIscd(), ""))) {

                                log.info("✅ 호가 데이터 매칭 - StockCode: {}, AskPrice1: {}, BidPrice1: {}",
                                        stockCode, orderbookData.getAskp1(), orderbookData.getBidp1());

                                // 데이터 핸들러를 통해 KisRealtimeService로 데이터 전달
                                dataHandler.accept(orderbookData);
                            } else {
                                log.debug("⏭️ 호가 데이터 스킵 - 요청: {}, 수신: {}",
                                        stockCode, orderbookData.getMkscShrnIscd());
                            }
                        }
                    } else if (data instanceof H0STASP0Data) {
                        // 단일 데이터 처리 (하위 호환성)
                        H0STASP0Data orderbookData = (H0STASP0Data) data;

                        // 종목코드 필드들 모두 로깅
                        log.info("📊 호가 단일 데이터 상세 - StockCode요청: {}, MkscShrnIscd: {}, StckShrnIscd: {}, AskPrice1: {}, BidPrice1: {}",
                                stockCode, orderbookData.getMkscShrnIscd(),
                                getFieldSafely(() -> orderbookData.getMkscShrnIscd(), "N/A"),
                                orderbookData.getAskp1(), orderbookData.getBidp1());

                        // 종목코드 필터링 (해당 종목만 처리)
                        if (stockCode.equals(orderbookData.getMkscShrnIscd()) ||
                            stockCode.equals(getFieldSafely(() -> orderbookData.getMkscShrnIscd(), ""))) {

                            log.info("✅ 호가 데이터 매칭 - StockCode: {}, AskPrice1: {}, BidPrice1: {}",
                                    stockCode, orderbookData.getAskp1(), orderbookData.getBidp1());

                            // 데이터 핸들러를 통해 KisRealtimeService로 데이터 전달
                            dataHandler.accept(orderbookData);
                        } else {
                            log.debug("⏭️ 호가 데이터 스킵 - 요청: {}, 수신: {}",
                                    stockCode, orderbookData.getMkscShrnIscd());
                        }
                    } else {
                        log.debug("🔍 호가 핸들러: 다른 타입 데이터 - Type: {}",
                            data != null ? data.getClass().getSimpleName() : "null");
                    }
                } catch (Exception e) {
                    log.error("❌ 실시간 호가 데이터 처리 중 오류 - StockCode: {}", stockCode, e);
                }
            });

            log.info("✅ 호가 핸들러 추가 완료 - StockCode: {}", stockCode);

        } catch (Exception e) {
            log.error("❌ 호가 핸들러 추가 실패 - StockCode: {}", stockCode, e);
            throw new RuntimeException("호가 핸들러 추가 실패", e);
        }
    }

    /**
     * WebSocket 상태 충돌 방지를 위한 재시도 로직 (KIS 라이브러리 RateLimiter 활용)
     */
    private <T> T executeWithRetry(Supplier<T> operation, int maxRetries, String operationName) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("🔄 {} 시도 {}/{}", operationName, attempt, maxRetries);
                return operation.get();
            } catch (Exception e) {
                lastException = e;

                // KIS 라이브러리 표준 예외 처리 패턴 활용
                boolean isRetryableError = false;
                long waitTime = 500L * attempt; // 기본 대기 시간

                // KisClientException 우선 처리
                if (e instanceof KisClientException) {
                    KisClientException kisException = (KisClientException) e;
                    log.warn("KIS API 에러 발생 - 시도: {}/{}, 에러 코드: {}, 메시지: {}",
                            attempt, maxRetries, kisException.getClass().getSimpleName(), kisException.getMessage());

                    // KIS API 에러는 대부분 재시도 가능
                    isRetryableError = true;
                    waitTime = 1000L * attempt; // KIS API 에러는 더 긴 대기
                }
                // WebSocket 상태 충돌 에러
                if (e.getMessage() != null &&
                    (e.getMessage().contains("TEXT_FULL_WRITING") ||
                     e.getMessage().contains("Invalid state") ||
                     e.getCause() instanceof IllegalStateException)) {
                    isRetryableError = true;
                    log.warn("⚠️ {} WebSocket 상태 충돌 발생 (시도 {}/{}), {}ms 후 재시도: {}",
                        operationName, attempt, maxRetries, waitTime, e.getMessage());
                }
                // API 호출 한도 초과 에러
                else if (e.getMessage() != null &&
                    (e.getMessage().contains("초당 거래건수를 초과") ||
                     e.getMessage().contains("EGW00201"))) {
                    isRetryableError = true;
                    waitTime = 1000L * attempt; // API 한도 초과 시 더 긴 대기 시간 (1초, 2초, 3초)
                    log.warn("⚠️ {} API 호출 한도 초과 (시도 {}/{}), {}ms 후 재시도: {}",
                        operationName, attempt, maxRetries, waitTime, e.getMessage());
                }
                // WebSocket 메시지 크기 초과 에러
                else if (e.getMessage() != null &&
                    (e.getMessage().contains("too big for the output buffer") ||
                     e.getMessage().contains("message was too big") ||
                     e.getMessage().contains("1009"))) {
                    isRetryableError = true;
                    waitTime = 2000L * attempt; // 메시지 크기 초과 시 더 긴 대기 (2초, 4초, 6초)
                    log.warn("⚠️ {} WebSocket 메시지 크기 초과 (시도 {}/{}), {}ms 후 재시도: {}",
                        operationName, attempt, maxRetries, waitTime, e.getMessage());
                }
                // AppKey 중복 사용 에러 (OPSP8996)
                else if (e.getMessage() != null &&
                    (e.getMessage().contains("OPSP8996") ||
                     e.getMessage().contains("ALREADY IN USE appkey"))) {
                    // AppKey 중복 에러는 재시도하지 않고 즉시 실패 (아키텍처 문제)
                    isRetryableError = false;
                    log.error("🚨 {} AppKey 중복 사용 에러 - 단일 연결 아키텍처 필요: {}",
                        operationName, e.getMessage());
                }

                if (isRetryableError && attempt < maxRetries) {

                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 중 인터럽트 발생", ie);
                    }
                } else if (!isRetryableError) {
                    // 재시도 불가능한 에러인 경우 즉시 실패
                    log.error("❌ {} 재시도 불가능한 에러로 즉시 실패: {}", operationName, e.getMessage());
                    break;
                } else {
                    log.error("❌ {} 최대 재시도 횟수 초과", operationName);
                }
            }
        }

        throw new RuntimeException(operationName + " 실패 (최대 " + maxRetries + "회 재시도)", lastException);
    }

    /**
     * 안전한 필드 접근 헬퍼
     */
    private String getFieldSafely(Supplier<String> supplier, String defaultValue) {
        try {
            String value = supplier.get();
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * WebSocket 연결 상태 검증 (현재 사용 안함 - API 호출 한도 절약을 위해)
     * 필요 시 활성화하여 사용 가능
     */
    @SuppressWarnings("unused")
    private void validateWebSocketConnection(KisClient client, String credentialsName, AccountType accountType) {
        try {
            log.info("🔍 WebSocket 연결 상태 검증 시작 - AccountType: {}", accountType);

            // KIS 클라이언트의 WebSocket 연결 상태 확인
            // 실제로는 KIS API 라이브러리에서 제공하는 연결 상태 확인 메서드를 사용해야 하지만,
            // 현재는 간단한 REST API 호출로 인증 상태를 확인
            InquireBalanceApi balanceApi = new InquireBalanceApi();
            if (accountType == AccountType.MOCK) {
                balanceApi.setTrId("VTTC8434R");
            }

            InquireBalanceResult result = client.execute(balanceApi, credentialsName);

            if (result == null || !result.getRtCd().equals("0")) {
                throw new RuntimeException("KIS API 인증 상태 불량: " +
                    (result != null ? result.getMsg1() : "응답 없음"));
            }

            log.info("✅ WebSocket 연결 상태 검증 완료 - AccountType: {}", accountType);

        } catch (Exception e) {
            log.error("❌ WebSocket 연결 상태 검증 실패 - AccountType: {}, Error: {}",
                accountType, e.getMessage());
            throw new RuntimeException("WebSocket 연결 상태 불량", e);
        }
    }

    /**
     * 실시간 체결가 데이터 처리 백그라운드 스레드 시작
     */
    private void startRealtimePriceProcessing(SubscribableApiResult subscription,
                                              String stockCode,
                                              Consumer<H0STCNT0Data> dataHandler) {
        try {
            log.info("실시간 체결가 데이터 처리 시작 - StockCode: {}", stockCode);

            // KIS API 라이브러리의 실제 콜백 메커니즘 구현
            // SubscribableApiResult를 통해 실시간 데이터 수신 처리
            subscription.addHandler(data -> {
                try {
                    if (data instanceof H0STCNT0Data) {
                        H0STCNT0Data priceData = (H0STCNT0Data) data;

                        log.debug("실시간 체결가 데이터 수신 - StockCode: {}, Price: {}",
                                stockCode, priceData.getStckPrpr());

                        // 데이터 핸들러를 통해 KisRealtimeService로 데이터 전달
                        dataHandler.accept(priceData);
                    }
                } catch (Exception e) {
                    log.error("실시간 체결가 데이터 처리 중 오류 - StockCode: {}", stockCode, e);
                }
            });

            log.info("실시간 체결가 데이터 수신 대기 중 - StockCode: {}", stockCode);

        } catch (Exception e) {
            log.error("실시간 체결가 데이터 처리 시작 실패 - StockCode: {}", stockCode, e);
            throw new RuntimeException("실시간 체결가 데이터 처리 실패", e);
        }
    }

    /**
     * 실시간 호가 데이터 처리 백그라운드 스레드 시작
     */
    private void startRealtimeOrderbookProcessing(SubscribableApiResult subscription,
                                                  String stockCode,
                                                  Consumer<H0STASP0Data> dataHandler) {
        try {
            log.info("실시간 호가 데이터 처리 시작 - StockCode: {}", stockCode);

            // KIS API 라이브러리의 실제 콜백 메커니즘 구현
            // SubscribableApiResult를 통해 실시간 데이터 수신 처리
            subscription.addHandler(data -> {
                try {
                    if (data instanceof H0STASP0Data) {
                        H0STASP0Data orderbookData = (H0STASP0Data) data;

                        log.debug("실시간 호가 데이터 수신 - StockCode: {}, AskPrice1: {}, BidPrice1: {}",
                                stockCode, orderbookData.getAskp1(), orderbookData.getBidp1());

                        // 데이터 핸들러를 통해 KisRealtimeService로 데이터 전달
                        dataHandler.accept(orderbookData);
                    }
                } catch (Exception e) {
                    log.error("실시간 호가 데이터 처리 중 오류 - StockCode: {}", stockCode, e);
                }
            });

            log.info("실시간 호가 데이터 수신 대기 중 - StockCode: {}", stockCode);

        } catch (Exception e) {
            log.error("실시간 호가 데이터 처리 시작 실패 - StockCode: {}", stockCode, e);
            throw new RuntimeException("실시간 호가 데이터 처리 실패", e);
        }
    }

    /**
     * 주식 매수 주문 실행 (Account 객체 사용)
     */
    public com.youhogeon.finance.kis_api.api.rest.trading.OrderCashResult executeBuyOrder(
            Account account, String stockCode, String orderType, int quantity, Long price) {
        Long userId = account.getUser().getId();
        DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
        ensureUserCredentials(userId, account.getId(), account.getAccountType(), credentials);
        return executeBuyOrder(userId, account.getId(), account.getAccountType(), stockCode, orderType, quantity, price);
    }

    /**
     * 주식 매수 주문 실행
     */
    public com.youhogeon.finance.kis_api.api.rest.trading.OrderCashResult executeBuyOrder(
            Long userId, Long accountId, AccountType accountType,
            String stockCode, String orderType, int quantity, Long price) {

        try {
            String credentialsName = getUserCredentialsName(userId, accountId);
            if (credentialsName == null) {
                throw new RuntimeException("등록된 Credentials를 찾을 수 없음");
            }

            KisClient client = accountType == AccountType.MOCK ? mockClient : realClient;

            com.youhogeon.finance.kis_api.api.rest.trading.OrderCashApi orderApi =
                    new com.youhogeon.finance.kis_api.api.rest.trading.OrderCashApi();

            // TR ID 설정
            if (accountType == AccountType.MOCK) {
                orderApi.setTrId("VTTC0012U");  // 모의투자 매수
            } else {
                orderApi.setTrId("TTTC0012U");  // 실거래 매수
            }

            // 주문 파라미터 설정
            orderApi.setPdno(stockCode);
            orderApi.setOrdDvsn(orderType);
            orderApi.setOrdQty(String.valueOf(quantity));
            orderApi.setOrdUnpr(price != null ? String.valueOf(price) : "0");
            orderApi.setExcgIdDvsnCd("KRX");

            // KIS API 실행 (credentialsName으로 자동 인증)
            com.youhogeon.finance.kis_api.api.rest.trading.OrderCashResult result =
                    client.execute(orderApi, credentialsName);

            log.info("주식 매수 주문 완료 - UserId: {}, StockCode: {}", userId, stockCode);
            return result;

        } catch (Exception e) {
            log.error("주식 매수 주문 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
            throw new RuntimeException("주식 매수 주문 실패", e);
        }
    }

    /**
     * 주식 매도 주문 실행 (Account 객체 사용)
     */
    public com.youhogeon.finance.kis_api.api.rest.trading.OrderCashResult executeSellOrder(
            Account account, String stockCode, String orderType, int quantity, Long price) {
        Long userId = account.getUser().getId();
        DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
        ensureUserCredentials(userId, account.getId(), account.getAccountType(), credentials);
        return executeSellOrder(userId, account.getId(), account.getAccountType(), stockCode, orderType, quantity, price);
    }

    /**
     * 주식 매도 주문 실행
     */
    public com.youhogeon.finance.kis_api.api.rest.trading.OrderCashResult executeSellOrder(
            Long userId, Long accountId, AccountType accountType,
            String stockCode, String orderType, int quantity, Long price) {

        try {
            String credentialsName = getUserCredentialsName(userId, accountId);
            if (credentialsName == null) {
                throw new RuntimeException("등록된 Credentials를 찾을 수 없음");
            }

            KisClient client = accountType == AccountType.MOCK ? mockClient : realClient;

            com.youhogeon.finance.kis_api.api.rest.trading.OrderCashApi orderApi =
                    new com.youhogeon.finance.kis_api.api.rest.trading.OrderCashApi();

            // TR ID 설정
            if (accountType == AccountType.MOCK) {
                orderApi.setTrId("VTTC0011U");  // 모의투자 매도
            } else {
                orderApi.setTrId("TTTC0011U");  // 실거래 매도
            }

            // 주문 파라미터 설정
            orderApi.setPdno(stockCode);
            orderApi.setOrdDvsn(orderType);
            orderApi.setOrdQty(String.valueOf(quantity));
            orderApi.setOrdUnpr(price != null ? String.valueOf(price) : "0");
            orderApi.setExcgIdDvsnCd("KRX");

            // KIS API 실행 (credentialsName으로 자동 인증)
            com.youhogeon.finance.kis_api.api.rest.trading.OrderCashResult result =
                    client.execute(orderApi, credentialsName);

            log.info("주식 매도 주문 완료 - UserId: {}, StockCode: {}", userId, stockCode);
            return result;

        } catch (Exception e) {
            log.error("주식 매도 주문 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
            throw new RuntimeException("주식 매도 주문 실패", e);
        }
    }

    /**
     * 국내주식기간별시세(일/주/월/년) 조회 (Account 객체 사용)
     */
    public Map<String, Object> getStockChartData(Account account, String stockCode, String startDate, String endDate,
                                                 String periodType) {
        Long userId = account.getUser().getId();
        DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
        ensureUserCredentials(userId, account.getId(), account.getAccountType(), credentials);
        return getStockChartData(userId, account.getId(), account.getAccountType(), stockCode, startDate, endDate, periodType);
    }

    /**
     * 국내주식기간별시세(일/주/월/년) 조회 KIS API의 FHKST03010100 TR ID 사용
     */
    public Map<String, Object> getStockChartData(Long userId, Long accountId, AccountType accountType,
                                                 String stockCode, String startDate, String endDate,
                                                 String periodType) {
        try {
            String credentialsName = getUserCredentialsName(userId, accountId);
            if (credentialsName == null) {
                throw new RuntimeException("등록된 Credentials를 찾을 수 없음");
            }

            KisClient client = accountType == AccountType.MOCK ? mockClient : realClient;

            // 실제 KIS 라이브러리의 InquireDailyItemchartpriceApi 사용
            InquireDailyItemchartpriceApi chartApi = new InquireDailyItemchartpriceApi();

            // KIS API 파라미터 설정
            chartApi.setFidCondMrktDivCode("J");      // J:KRX, NX:NXT, UN:통합
            chartApi.setFidInputIscd(stockCode);       // 종목코드 (ex 005930)
            chartApi.setFidInputDate1(startDate);      // 조회 시작일자
            chartApi.setFidInputDate2(endDate);        // 조회 종료일자
            chartApi.setFidPeriodDivCode(periodType);  // D:일봉 W:주봉, M:월봉, Y:년봉
            chartApi.setFidOrgAdjPrc("1");             // 0:수정주가 1:원주가

            // P:개인, B:법인
            chartApi.setCusttype("P");

            log.info("KIS 차트 API 파라미터 설정 완료 - StockCode: {}, StartDate: {}, EndDate: {}, Period: {}, AccountType: {}",
                    stockCode, startDate, endDate, periodType, accountType);

            // KIS API 실행 (기존 패턴과 동일)
            InquireDailyItemchartpriceResult result = client.execute(chartApi, credentialsName);

            // 실무 표준: KIS API 응답 코드 검증
            if (result == null) {
                throw new RuntimeException("KIS API 응답이 null입니다.");
            }

            // rtCd가 "0"이 아니면 실패 (KIS API 표준)
            if (!"0".equals(result.getRtCd())) {
                String errorMsg = String.format("KIS 차트 API 실패 - 응답코드: %s, 메시지: %s",
                        result.getRtCd(), result.getMsg1());
                log.error("차트 데이터 조회 실패 - UserId: {}, StockCode: {}, {}", userId, stockCode, errorMsg);
                throw new RuntimeException(errorMsg);
            }

            if (result != null) {
                Map<String, Object> responseData = new ConcurrentHashMap<>();

                // output1 (종목 요약 정보)
                if (result.getOutput1() != null) {
                    responseData.put("output1", result.getOutput1());
                }

                // output2 (차트 데이터 배열) - 원본 배열 그대로 사용
                if (result.getOutput2() != null) {
                    responseData.put("output2", result.getOutput2());
                }

                log.info("주식 차트 데이터 조회 완료 (KIS API) - UserId: {}, StockCode: {}, Period: {}",
                        userId, stockCode, periodType);

                return responseData;
            } else {
                log.error("KIS API 응답이 null - UserId: {}, StockCode: {}, Period: {}", userId, stockCode, periodType);
                throw new RuntimeException("KIS API에서 차트 데이터를 가져올 수 없습니다. 잠시 후 다시 시도해주세요.");
            }

        } catch (KisClientException e) {
            // KIS 라이브러리 전용 예외 처리
            log.error("KIS API 클라이언트 오류 - UserId: {}, StockCode: {}, Period: {}, 오류: {}",
                    userId, stockCode, periodType, e.getMessage(), e);
            throw new RuntimeException("KIS API 연결 오류: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("주식 차트 데이터 조회 실패 - UserId: {}, StockCode: {}, Period: {}, Error: {}, StackTrace: {}",
                    userId, stockCode, periodType, e.getMessage(), e.getClass().getSimpleName(), e);

            // KIS API 호출 실패 시 예외를 다시 던져서 상위 레이어에서 처리하도록 함
            throw new RuntimeException("KIS API 차트 데이터 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 거래량순위 조회 (Account 객체 사용)
     */
    public VolumeRankResult getVolumeRanking(Account account) {
        Long userId = account.getUser().getId();
        DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
        return getVolumeRanking(userId, account.getId(), account.getAccountType(), credentials);
    }

    /**
     * 거래량순위 조회
     */
    public VolumeRankResult getVolumeRanking(Long userId, Long accountId, AccountType accountType,
                                             DecryptedAccountCredentials credentials) {
        try {
            log.info("거래량순위 조회 시작 - 사용자ID: {}, 계좌ID: {}, 계좌타입: {}", userId, accountId, accountType);

            // Credentials가 Config에 없으면 자동으로 등록
            ensureUserCredentials(userId, accountId, accountType, credentials);

            String credentialsName = getUserCredentialsName(userId, accountId);
            if (credentialsName == null) {
                log.error("ensureUserCredentials 후에도 Credentials를 찾을 수 없음 - 사용자ID: {}, 계좌ID: {}", userId, accountId);
                throw new RuntimeException("Credentials 등록 실패");
            }

            KisClient client = accountType == AccountType.MOCK ? mockClient : realClient;

            VolumeRank req = new VolumeRank();
            // 기본 파라미터는 이미 설정되어 있음

            VolumeRankResult result = client.execute(req, credentialsName);
            System.out.println(result.toString());
            // rtCd가 "0"이 아니면 실패 (KIS API 표준)
            if (!result.getRtCd().equals("0")) {
                throw new RuntimeException("KIS API 거래량순위 조회 실패");
            }

            log.info("거래량순위 조회 완료 - 사용자ID: {}, 계좌ID: {}", userId, accountId);
            return result;

        } catch (KisException e) {
            // KisException은 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("거래량순위 조회 실패 - 사용자ID: {}, 계좌ID: {}, 오류: {}",
                    userId, accountId, e.getMessage(), e);
            throw new RuntimeException("거래량순위 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 등락률순위 조회 - 급상승 (Account 객체 사용)
     */
    public FluctuationRankingResult getFluctuationRankingRising(Account account) {
        Long userId = account.getUser().getId();
        DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
        return getFluctuationRanking(userId, account.getId(), account.getAccountType(), credentials, true);
    }

    /**
     * 등락률순위 조회 - 급하락 (Account 객체 사용)
     */
    public FluctuationRankingResult getFluctuationRankingFalling(Account account) {
        Long userId = account.getUser().getId();
        DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);
        return getFluctuationRanking(userId, account.getId(), account.getAccountType(), credentials, false);
    }

    /**
     * 등락률순위 조회 (급상승/급하락 구분)
     */
    public FluctuationRankingResult getFluctuationRanking(Long userId, Long accountId, AccountType accountType,
                                                          DecryptedAccountCredentials credentials, boolean isRising) {
        try {
            String rankingType = isRising ? "급상승" : "급하락";
            log.info("{} 등락률순위 조회 시작 - 사용자ID: {}, 계좌ID: {}, 계좌타입: {}",
                    rankingType, userId, accountId, accountType);

            // Credentials가 Config에 없으면 자동으로 등록
            ensureUserCredentials(userId, accountId, accountType, credentials);

            String credentialsName = getUserCredentialsName(userId, accountId);
            if (credentialsName == null) {
                throw new RuntimeException("Credentials 등록 실패");
            }

            KisClient client = accountType == AccountType.MOCK ? mockClient : realClient;

            FluctuationRanking req = new FluctuationRanking();

            // 급상승/급하락별 파라미터 설정
            if (!isRising) {
                System.out.println("급하락");
                req.setFidRankSortClsCode("1");
                req.setFidPrcClsCode("11");
            }

//            req.setFidInputCnt_1("10");

            FluctuationRankingResult result = client.execute(req, credentialsName);

            System.out.println(result.toString());
            // rtCd가 "0"이 아니면 실패 (KIS API 표준)
            if (!result.getRtCd().equals("0")) {
                throw new RuntimeException("KIS API " + rankingType + " 등락률순위 조회 실패");
            }

            log.info("{} 등락률순위 조회 완료 - 사용자ID: {}, 계좌ID: {}", rankingType, userId, accountId);
            return result;

        } catch (KisException e) {
            // KisException은 그대로 전파
            throw e;
        } catch (Exception e) {
            String rankingType = isRising ? "급상승" : "급하락";
            log.error("{} 등락률순위 조회 실패 - 사용자ID: {}, 계좌ID: {}, 오류: {}",
                    rankingType, userId, accountId, e.getMessage(), e);
            throw new RuntimeException(rankingType + " 등락률순위 조회 실패: " + e.getMessage(), e);
        }
    }



}