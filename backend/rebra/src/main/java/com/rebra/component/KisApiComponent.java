package com.rebra.component;

import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.entity.AccountType;
import com.youhogeon.finance.kis_api.KisClient;
import com.youhogeon.finance.kis_api.api.realtime.H0STASP0Api;
import com.youhogeon.finance.kis_api.api.realtime.H0STASP0Data;
import com.youhogeon.finance.kis_api.api.realtime.H0STCNT0Api;
import com.youhogeon.finance.kis_api.api.realtime.H0STCNT0Data;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceApi;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import com.youhogeon.finance.kis_api.client.socket.SubscribableApiResult;
import com.youhogeon.finance.kis_api.config.Configuration;
import com.youhogeon.finance.kis_api.config.Credentials;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * KIS API 클라이언트 및 연결 관리 컴포넌트
 */
@Slf4j
@Component
public class KisApiComponent {

    private Configuration mockConfig;  // 모의투자용 Configuration
    private Configuration realConfig;  // 실계좌용 Configuration
    private KisClient mockClient;      // 모의투자용 Client
    private KisClient realClient;      // 실계좌용 Client

    // 사용자별 Credentials 명 관리
    private final Map<String, String> userCredentialsMap = new ConcurrentHashMap<>();

    // 실시간 구독 관리
    private final Map<String, AtomicInteger> subscriptionCount = new ConcurrentHashMap<>();
    private final Map<String, SubscribableApiResult> activeSubscriptions = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeConfigurations() {
        log.info("KIS API Configuration 초기화 시작");

        // 모의투자용 Configuration 생성
        mockConfig = new Configuration();
        mockConfig.setHttpHost("https://openapivts.koreainvestment.com:29443");
        mockConfig.setHttpTimeout(Duration.ofSeconds(30));
        mockConfig.setHttpTimeoutMaxRetries(3);
        mockConfig.setSocketHost("ws://ops.koreainvestment.com:31000");

        // 실계좌용 Configuration 생성
        realConfig = new Configuration();
        realConfig.setHttpHost("https://openapi.koreainvestment.com:9443");
        realConfig.setHttpTimeout(Duration.ofSeconds(30));
        realConfig.setHttpTimeoutMaxRetries(3);
        realConfig.setSocketHost("ws://ops.koreainvestment.com:21000");

        // KisClient 생성
        mockClient = new KisClient(mockConfig);
        realClient = new KisClient(realConfig);

        log.info("KIS API Configuration 초기화 완료");
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
            log.info("KIS API 연결 테스트 시작 - 계좌번호: {}", accountNumber);

            Credentials credentials = new Credentials(appKey, appSecret, accountNumber, accountNumber);

            Configuration config = new Configuration();
            config.addCredentials(credentials);

            // 모의투자와 실계좌에 따른 trId 설정
            if (accountType == AccountType.MOCK) {
                config.setHttpHost("https://openapivts.koreainvestment.com:29443");
            }

            KisClient client = new KisClient(config);

            // 잔고조회 API로 연결 테스트
            InquireBalanceApi req = new InquireBalanceApi();

            // 모의투자와 실계좌에 따른 trId 설정
            if (accountType == AccountType.MOCK) {
                req.setTrId("VTTC8434R");
            }

            InquireBalanceResult result = client.execute(req);

            if (result == null) {
                throw new RuntimeException("KIS API 연결 실패: 응답 데이터 없음");
            }
        } catch (Exception e) {
            log.error("KIS API 연결 테스트 중 예외 발생 - 계좌번호: {}, 오류: {}",
                    accountNumber, e.getMessage(), e);
            throw new RuntimeException("KIS API 연결 실패", e);
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
     * 등록된 사용자 Credentials로 잔고 조회
     * ensureUserCredentials()를 통해 Credentials가 없으면 자동으로 등록
     */
    public InquireBalanceResult getUserBalance(Long userId, Long accountId, AccountType accountType, DecryptedAccountCredentials credentials) {
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

            return result;
        } catch (Exception e) {
            log.error("사용자 잔고 조회 실패 - 사용자ID: {}, 계좌ID: {}, 오류: {}",
                    userId, accountId, e.getMessage(), e);
            throw new RuntimeException("사용자 잔고 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 실시간 체결가 구독 시작
     */
    public void startPriceSubscription(Long userId, Long accountId, String stockCode,
                                       AccountType accountType, Consumer<H0STCNT0Data> dataHandler) {
        try {
            String credentialsName = getUserCredentialsName(userId, accountId);
            if (credentialsName == null) {
                throw new RuntimeException("등록된 Credentials를 찾을 수 없음");
            }

            String subscriptionKey = generateSubscriptionKey(userId, stockCode, "price");

            // 구독 참조 카운트 증가
            int count = subscriptionCount.computeIfAbsent(subscriptionKey, k -> new AtomicInteger(0)).incrementAndGet();

            // 첫 번째 구독인 경우에만 KIS WebSocket 연결 시작
            if (count == 1) {
                KisClient client = accountType == AccountType.MOCK ? mockClient : realClient;
                H0STCNT0Api priceApi = new H0STCNT0Api(stockCode);

                // 실제 KIS WebSocket 구독 시작
                try {
                    SubscribableApiResult subscription = client.execute(priceApi, credentialsName);

                    // KIS WebSocket 실시간 데이터 구독
                    // SubscribableApiResult는 WebSocket 연결을 관리하는 객체
                    log.info("KIS WebSocket 실시간 체결가 구독 성공 - StockCode: {}", stockCode);

                    // 실제 실시간 데이터 처리를 위한 백그라운드 스레드 시작
                    startRealtimePriceProcessing(subscription, stockCode, dataHandler);

                    activeSubscriptions.put(subscriptionKey, subscription);

                    log.info("실시간 체결가 구독 시작 - UserId: {}, StockCode: {}, 구독자: {}명",
                            userId, stockCode, count);
                } catch (Exception e) {
                    log.error("KIS WebSocket 구독 시작 실패 - UserId: {}, StockCode: {}, Error: {}",
                            userId, stockCode, e.getMessage(), e);
                    throw new RuntimeException("KIS 실시간 체결가 구독 실패", e);
                }
            } else {
                log.info("기존 체결가 구독에 참여 - UserId: {}, StockCode: {}, 구독자: {}명",
                        userId, stockCode, count);
            }

        } catch (Exception e) {
            log.error("실시간 체결가 구독 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
            throw new RuntimeException("실시간 체결가 구독 실패", e);
        }
    }

    /**
     * 실시간 호가 구독 시작
     */
    public void startOrderbookSubscription(Long userId, Long accountId, String stockCode,
                                           AccountType accountType, Consumer<H0STASP0Data> dataHandler) {
        try {
            String credentialsName = getUserCredentialsName(userId, accountId);
            if (credentialsName == null) {
                throw new RuntimeException("등록된 Credentials를 찾을 수 없음");
            }

            String subscriptionKey = generateSubscriptionKey(userId, stockCode, "orderbook");

            // 구독 참조 카운트 증가
            int count = subscriptionCount.computeIfAbsent(subscriptionKey, k -> new AtomicInteger(0)).incrementAndGet();

            // 첫 번째 구독인 경우에만 KIS WebSocket 연결 시작
            if (count == 1) {
                KisClient client = accountType == AccountType.MOCK ? mockClient : realClient;
                H0STASP0Api orderbookApi = new H0STASP0Api(stockCode);

                // 실제 KIS WebSocket 구독 시작
                try {
                    SubscribableApiResult subscription = client.execute(orderbookApi, credentialsName);

                    // KIS WebSocket 실시간 데이터 구독
                    // SubscribableApiResult는 WebSocket 연결을 관리하는 객체
                    log.info("KIS WebSocket 실시간 호가 구독 성공 - StockCode: {}", stockCode);

                    // 실제 실시간 데이터 처리를 위한 백그라운드 스레드 시작
                    startRealtimeOrderbookProcessing(subscription, stockCode, dataHandler);

                    activeSubscriptions.put(subscriptionKey, subscription);

                    log.info("실시간 호가 구독 시작 - UserId: {}, StockCode: {}, 구독자: {}명",
                            userId, stockCode, count);
                } catch (Exception e) {
                    log.error("KIS WebSocket 구독 시작 실패 - UserId: {}, StockCode: {}, Error: {}",
                            userId, stockCode, e.getMessage(), e);
                    throw new RuntimeException("KIS 실시간 호가 구독 실패", e);
                }
            } else {
                log.info("기존 호가 구독에 참여 - UserId: {}, StockCode: {}, 구독자: {}명",
                        userId, stockCode, count);
            }

        } catch (Exception e) {
            log.error("실시간 호가 구독 실패 - UserId: {}, StockCode: {}", userId, stockCode, e);
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
     * 활성 구독 상태 확인
     */
    public boolean isSubscribed(Long userId, String stockCode, String dataType) {
        String subscriptionKey = generateSubscriptionKey(userId, stockCode, dataType);
        return subscriptionCount.containsKey(subscriptionKey) &&
                subscriptionCount.get(subscriptionKey).get() > 0;
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
}