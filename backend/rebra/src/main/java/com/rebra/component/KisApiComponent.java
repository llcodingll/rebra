package com.rebra.component;

import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.entity.AccountType;
import com.youhogeon.finance.kis_api.*;
import com.youhogeon.finance.kis_api.config.Configuration;
import com.youhogeon.finance.kis_api.config.Credentials;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceApi;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

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

    @PostConstruct
    public void initializeConfigurations() {
        log.info("KIS API Configuration 초기화 시작");

        // 모의투자용 Configuration 생성
        mockConfig = new Configuration();
        mockConfig.setHttpHost("https://openapivts.koreainvestment.com:29443");
        mockConfig.setHttpTimeout(Duration.ofSeconds(30));
        mockConfig.setHttpTimeoutMaxRetries(3);

        // 실계좌용 Configuration 생성
        realConfig = new Configuration();
        realConfig.setHttpHost("https://openapi.koreainvestment.com:9443");
        realConfig.setHttpTimeout(Duration.ofSeconds(30));
        realConfig.setHttpTimeoutMaxRetries(3);

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
     * DB O / Config X => 이 경우 엔티티를 통해서 Credentials를 Config에 바로 저장합니다.
     * Config는 항상 초기화 하면 안됨 -> 액세스 토큰이 들어있음
     * 항상 Credentials 등록 시엔 verifyAccount로 해당 앱키가 유효한지 확인해야함!
     */
    public void ensureUserCredentials(Long userId, Long accountId, AccountType accountType, DecryptedAccountCredentials credentials) {
        if(userCredentialsMap.containsKey(generateCredentialsName(userId, accountId))) return;

        addUserCredentials(userId, accountId, credentials.getAccountNumber(), credentials.getAppKey(), credentials.getAppSecret(),
                accountType);
    }

    /**
     * 사용자 Credentials 제거
     */
    public void removeUserCredentials(Long userId, Long accountId, AccountType accountType) {
        String userKey = generateCredentialsName(userId, accountId);
        String credentialsName = userCredentialsMap.get(userKey);

        if(credentialsName == null) return;

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
            if(accountType == AccountType.MOCK) {
                config.setHttpHost("https://openapivts.koreainvestment.com:29443");
            }

            KisClient client = new KisClient(config);

            // 잔고조회 API로 연결 테스트
            InquireBalanceApi req = new InquireBalanceApi();

            // 모의투자와 실계좌에 따른 trId 설정
            if(accountType == AccountType.MOCK) {
                req.setTrId("VTTC8434R");
            }

            InquireBalanceResult result = client.execute(req);

            if(result == null) {
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
     * @param accountCredentials
     * @param accountType
     */
    public void verifyAccount(DecryptedAccountCredentials accountCredentials,  AccountType accountType) {
        verifyAccount(accountCredentials.getAccountNumber(), accountCredentials.getAppKey(), accountCredentials.getAppSecret(), accountType);
    }


    /**
     * 등록된 사용자 Credentials로 잔고 조회
     */
    public InquireBalanceResult getUserBalance(Long userId, Long accountId, AccountType accountType) {
        try {
            String credentialsName = getUserCredentialsName(userId, accountId);
            if (credentialsName == null) {
                throw new RuntimeException("등록된 Credentials를 찾을 수 없음");
            }

            KisClient client = accountType ==  AccountType.MOCK ? mockClient : realClient;

            InquireBalanceApi req = new InquireBalanceApi();

            if(accountType == AccountType.MOCK) {
                req.setTrId("VTTC8434R");
            }

            InquireBalanceResult result = client.execute(req, credentialsName);

            log.info("사용자 잔고 조회 완료 - 사용자ID: {}, 계좌ID: {}", userId, accountId);
            return result;
        } catch (Exception e) {
            log.error("사용자 잔고 조회 실패 - 사용자ID: {}, 계좌ID: {}, 오류: {}",
                    userId, accountId, e.getMessage(), e);
            throw new RuntimeException("사용자 잔고 조회 실패", e);
        }
    }
}