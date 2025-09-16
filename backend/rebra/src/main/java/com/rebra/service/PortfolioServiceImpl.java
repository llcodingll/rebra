package com.rebra.service;

import com.rebra.dto.portfoliodata.PortfolioReturnData;
import com.rebra.dto.portfoliodata.PortfolioSummary;
import com.rebra.dto.request.PortfolioBasicUpdateRequest;
import com.rebra.dto.request.PortfolioCreateRequest;
import com.rebra.dto.request.PortfolioRebalancingSettingsRequest;
import com.rebra.dto.portfoliodata.RegisteredStockInfo;
import com.rebra.dto.portfoliodata.UnregisteredStockInfo;
import com.rebra.dto.response.PortfolioCreateResponse;
import com.rebra.dto.response.PortfolioDetailResponse;
import com.rebra.dto.response.PortfolioListResponse;
import com.rebra.dto.response.PortfolioUpdateResponse;
import com.rebra.component.KisApiComponent;
import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.entity.Account;
import com.rebra.entity.Portfolio;
import com.rebra.entity.PortfolioStock;
import com.rebra.entity.User;
import com.rebra.exception.portfolio.PortfolioException;
import com.rebra.exception.user.UserException;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.PortfolioStockRepository;
import com.rebra.repository.UserRepository;
import com.rebra.util.AccountEncryptionUtil;
import com.rebra.util.PortfolioCalculationUtil;
import com.youhogeon.finance.kis_api.api.rest.trading.InquireBalanceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockRepository portfolioStockRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final KisApiComponent kisApiComponent;

    /**
     * 최적화 예정 / 배치 쿼리
     * @param userId 사용자 ID
     * @return
     */
    @Override
    public PortfolioListResponse getPortfolioList(Long userId) {
        log.info("포트폴리오 목록 조회 시작 - 사용자ID: {}", userId);

        List<Portfolio> portfolios = portfolioRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<PortfolioSummary> portfolioSummaries = new ArrayList<>();

        for (int i = 0; i < portfolios.size(); i++) {
            Portfolio portfolio = portfolios.get(i);
            try {
                // 1. 계좌 정보 획득
                Account account = portfolio.getAccount();

                // 2. 등록 주식 목록 조회
                List<PortfolioStock> portfolioStocks = portfolioStockRepository
                    .findByPortfolioIdOrderByCreatedAtDesc(portfolio.getId());
                int stockCount = portfolioStocks.size();

                DecryptedAccountCredentials credentials = AccountEncryptionUtil
                    .decryptAccountCredentials(account, userId);
                InquireBalanceResult kisBalance = kisApiComponent.getUserBalance(
                    userId, account.getId(), account.getAccountType(), credentials);

                // 4. 포트폴리오 수익률 계산
                PortfolioReturnData returnData = PortfolioCalculationUtil.calculateReturn(portfolioStocks, kisBalance);

                // 5. DTO 생성
                portfolioSummaries.add(PortfolioSummary.of(portfolio, returnData, stockCount));

                log.info("포트폴리오 수익률 계산 완료 - 포트폴리오ID: {}, 수익률: {}%",
                    portfolio.getId(), returnData.getReturnRate());

            } catch (Exception e) {
                log.error("포트폴리오 수익률 계산 실패 - 포트폴리오ID: {}, 오류: {}",
                    portfolio.getId(), e.getMessage(), e);

                // 에러 발생 시 기본값으로 처리
                portfolioSummaries.add(PortfolioSummary.ofDefault(portfolio));
            }
        }

        log.info("포트폴리오 목록 조회 완료 - 사용자ID: {}, 포트폴리오 수: {}", userId, portfolios.size());

        return PortfolioListResponse.of(portfolioSummaries);
    }

    @Override
    @Transactional
    public PortfolioCreateResponse createPortfolio(Long userId, PortfolioCreateRequest request) {
        log.info("포트폴리오 생성 시작 - 사용자ID: {}, 포트폴리오명: {}", userId, request.getName());

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserException.userNotFound());

        Account account = accountRepository.findByIdAndUserId(request.getAccountId(), userId)
            .orElseThrow(() -> {
                log.error("계좌를 찾을 수 없음 - 사용자ID: {}, 계좌ID: {}", userId, request.getAccountId());
                return PortfolioException.accountNotFound();
            });

        Portfolio portfolio = Portfolio.builder()
            .user(user)
            .account(account)
            .name(request.getName())
            .description(request.getDescription())
            .build();

        portfolioRepository.save(portfolio);

        log.info("포트폴리오 생성 완료 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolio.getId());

        // 계좌번호 복호화하여 응답에 포함
        DecryptedAccountCredentials credentials = AccountEncryptionUtil
            .decryptAccountCredentials(account, userId);

        return PortfolioCreateResponse.of(portfolio, credentials.getAccountNumber());
    }

    @Override
    @Transactional
    public void deletePortfolio(Long userId, Long portfolioId) {
        log.info("포트폴리오 삭제 시작 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        Portfolio portfolio = findPortfolioByUserAndId(userId, portfolioId);

        // 연관된 포트폴리오 주식 데이터도 함께 삭제됨 (CASCADE 설정에 따라)

        portfolioRepository.delete(portfolio);

        log.info("포트폴리오 삭제 완료 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);
    }

    @Override
    public long getPortfolioCount(Long userId) {
        log.info("포트폴리오 개수 조회 - 사용자ID: {}", userId);

        long count = portfolioRepository.countByUserId(userId);

        log.info("포트폴리오 개수 조회 완료 - 사용자ID: {}, 개수: {}", userId, count);

        return count;
    }

    @Override
    @Transactional
    public PortfolioUpdateResponse updateAutoRebalancing(Long userId, Long portfolioId, Boolean autoRebalancing) {
        log.info("자동 리밸런싱 설정 변경 - 사용자ID: {}, 포트폴리오ID: {}, 설정: {}", userId, portfolioId, autoRebalancing);

        Portfolio portfolio = findPortfolioByUserAndId(userId, portfolioId);

        portfolio.updateAutoRebalancing(autoRebalancing);

        log.info("자동 리밸런싱 설정 변경 완료 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        String message = autoRebalancing ? "자동 리밸런싱이 활성화되었습니다." : "자동 리밸런싱이 비활성화되었습니다.";
        return PortfolioUpdateResponse.success(portfolioId, message);
    }

    @Override
    @Transactional
    public PortfolioUpdateResponse updateRebalancingSettings(Long userId, Long portfolioId, PortfolioRebalancingSettingsRequest request) {
        log.info("리밸런싱 설정 변경 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        Portfolio portfolio = findPortfolioByUserAndId(userId, portfolioId);

        portfolio.updateRebalancingSettings(
            request.getRebalancingStartDate(),
            request.getRebalancingPeriod(),
            request.getRebalancingInterval()
        );

        log.info("리밸런싱 설정 변경 완료 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        return PortfolioUpdateResponse.success(portfolioId, "리밸런싱 설정이 성공적으로 변경되었습니다.");
    }

    @Override
    @Transactional
    public PortfolioUpdateResponse updateBasicInfo(Long userId, Long portfolioId, PortfolioBasicUpdateRequest request) {
        log.info("포트폴리오 기본 정보 수정 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        Portfolio portfolio = findPortfolioByUserAndId(userId, portfolioId);

        portfolio.updateBasicInfo(request.getName(), request.getDescription());

        log.info("포트폴리오 기본 정보 수정 완료 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        return PortfolioUpdateResponse.of(
            portfolio.getId(),
            portfolio.getName(),
            portfolio.getDescription(),
            portfolio.getUpdatedAt()
        );
    }

    /**
     * 최적화 예정 / 패치조인
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     * @return
     */
    @Override
    public PortfolioDetailResponse getPortfolioDetail(Long userId, Long portfolioId) {
        log.info("포트폴리오 상세 조회 시작 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);

        try {
            // 1. 포트폴리오 조회 (권한 확인 포함)
            Portfolio portfolio = findPortfolioByUserAndId(userId, portfolioId);

            // 2. 등록된 주식 목록 조회 (PortfolioStock)
            List<PortfolioStock> portfolioStocks = portfolioStockRepository
                .findByPortfolioIdOrderByCreatedAtDesc(portfolioId);

            // 3. KIS API로 전체 잔고 조회
            Account account = portfolio.getAccount();
            DecryptedAccountCredentials credentials = AccountEncryptionUtil
                .decryptAccountCredentials(account, userId);
            InquireBalanceResult kisBalance = kisApiComponent.getUserBalance(
                userId, account.getId(), account.getAccountType(), credentials);

            // 4. 등록된 주식 코드별 PortfolioStock 맵 생성 (O(1) 검색을 위해)
            Map<String, PortfolioStock> portfolioStockMap = portfolioStocks.stream()
                .collect(Collectors.toMap(
                    PortfolioStock::getStockCode,
                    Function.identity()
                ));

            // 5. KIS 잔고를 등록/미등록으로 분류
            List<RegisteredStockInfo> registeredStocks = new ArrayList<>();
            List<UnregisteredStockInfo> unregisteredStocks = new ArrayList<>();

            for (InquireBalanceResult.Output1 balance : kisBalance.getOutput1()) {
                String stockCode = balance.getPdno();

                // 보유수량이 0이면 제외
                if (Long.parseLong(balance.getHldgQty()) == 0) {
                    continue;
                }

                if (portfolioStockMap.containsKey(stockCode)) {
                    // 등록된 주식 - PortfolioStock 정보와 결합
                    PortfolioStock ps = portfolioStockMap.get(stockCode);
                    registeredStocks.add(RegisteredStockInfo.from(balance,
                        ps.getTargetWeight(),
                        ps.getThresholdPercentage(),
                        ps.getStatus()));
                } else {
                    // 미등록 주식 - KIS 정보만
                    unregisteredStocks.add(UnregisteredStockInfo.from(balance));
                }
            }

            // 6. 계좌번호 복호화
            String accountNumber = credentials.getAccountNumber();

            // 7. 응답 생성
            PortfolioDetailResponse response = PortfolioDetailResponse.of(
                portfolio, accountNumber, registeredStocks, unregisteredStocks);

            log.info("포트폴리오 상세 조회 완료 - 사용자ID: {}, 포트폴리오ID: {}, 등록주식: {}개, 미등록주식: {}개",
                userId, portfolioId, registeredStocks.size(), unregisteredStocks.size());

            return response;

        } catch (Exception e) {
            log.error("포트폴리오 상세 조회 실패 - 사용자ID: {}, 포트폴리오ID: {}, 오류: {}",
                userId, portfolioId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 사용자별 포트폴리오 조회 (공통 로직)
     */
    private Portfolio findPortfolioByUserAndId(Long userId, Long portfolioId) {
        return portfolioRepository.findByIdAndUserId(portfolioId, userId)
            .orElseThrow(() -> {
                log.error("포트폴리오를 찾을 수 없음 - 사용자ID: {}, 포트폴리오ID: {}", userId, portfolioId);
                return PortfolioException.portfolioNotFound();
            });
    }
}