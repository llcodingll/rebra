package com.rebra.service;

import com.rebra.dto.request.PortfolioStockRegisterRequest;
import com.rebra.dto.request.PortfolioStockDeleteRequest;
import com.rebra.dto.request.PortfolioStockBatchUpdateRequest;
import com.rebra.dto.request.PortfolioStockUpdateRequest;
import com.rebra.dto.response.PortfolioStockResponse;
import com.rebra.entity.Portfolio;
import com.rebra.entity.PortfolioStock;
import com.rebra.exception.portfolio.PortfolioException;
import com.rebra.exception.portfoliostock.PortfolioStockException;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.PortfolioStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioStockServiceImpl implements PortfolioStockService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioStockRepository portfolioStockRepository;

    @Override
    @Transactional
    public PortfolioStockResponse registerStock(Long userId, Long portfolioId, PortfolioStockRegisterRequest request) {
        log.info("포트폴리오 주식 등록 시작 - 사용자ID: {}, 포트폴리오ID: {}, 주식코드: {}",
                userId, portfolioId, request.getStockCode());

        // 1. 포트폴리오 조회 및 권한 확인
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(PortfolioException::portfolioNotFound);

        // 2. 중복 등록 확인
        if (portfolioStockRepository.existsByPortfolioIdAndStockCode(portfolioId, request.getStockCode())) {
            throw PortfolioStockException.portfolioStockAlreadyExists();
        }

        // 3. PortfolioStock 생성 (targetWeight, thresholdPercentage, status 모두 null)
        PortfolioStock portfolioStock = PortfolioStock.builder()
                .portfolio(portfolio)
                .stockCode(request.getStockCode())
                .targetWeight(null)
                .thresholdPercentage(null)
                .status(null)
                .build();

        // 4. 저장
        PortfolioStock savedPortfolioStock = portfolioStockRepository.save(portfolioStock);

        // 5. 연관관계 편의 메서드 호출
        portfolio.addPortfolioStock(savedPortfolioStock);

        log.info("포트폴리오 주식 등록 완료 - 포트폴리오스탁ID: {}", savedPortfolioStock.getId());

        return PortfolioStockResponse.from(savedPortfolioStock, null);
    }

    @Override
    @Transactional
    public void deleteStock(Long userId, Long portfolioId, PortfolioStockDeleteRequest request) {
        log.info("포트폴리오 주식 삭제 시작 - 사용자ID: {}, 포트폴리오ID: {}, 주식코드: {}",
                userId, portfolioId, request.getStockCode());

        // 1. 포트폴리오 조회 및 권한 확인
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(PortfolioException::portfolioNotFound);

        // 2. PortfolioStock 조회 (포트폴리오 범위 내에서 stockCode로 조회)
        PortfolioStock portfolioStock = portfolioStockRepository.findByPortfolioIdAndStockCode(portfolioId, request.getStockCode())
                .orElseThrow(PortfolioStockException::portfolioStockNotFound);

        // 3. 연관관계 편의 메서드 호출
        portfolio.removePortfolioStock(portfolioStock);

        // 4. 삭제
        portfolioStockRepository.delete(portfolioStock);

        log.info("포트폴리오 주식 삭제 완료 - 주식코드: {}", request.getStockCode());
    }

    /**
     * 최적화 예정
     * @param userId 사용자 ID
     * @param portfolioId 포트폴리오 ID
     * @param request 일괄 업데이트 요청
     * @return
     */
    @Override
    @Transactional
    public List<PortfolioStockResponse> updateStocksBatch(Long userId, Long portfolioId, PortfolioStockBatchUpdateRequest request) {
        log.info("포트폴리오 주식 일괄 업데이트 시작 - 사용자ID: {}, 포트폴리오ID: {}, 업데이트 주식 수: {}",
                userId, portfolioId, request.getStocks().size());

        // 1. 포트폴리오 조회 및 권한 확인.. 흠
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(PortfolioException::portfolioNotFound);

        // 2. 요청된 stockCode들 추출
        List<String> requestedStockCodes = request.getStocks().stream()
                .map(PortfolioStockUpdateRequest::getStockCode)
                .collect(Collectors.toList());

        // 3. 해당 포트폴리오의 PortfolioStock들 조회
        List<PortfolioStock> portfolioStocks = new ArrayList<>();
        for (String stockCode : requestedStockCodes) {
            PortfolioStock portfolioStock = portfolioStockRepository.findByPortfolioIdAndStockCode(portfolioId, stockCode)
                    .orElseThrow(() -> {
                        log.error("포트폴리오에 등록되지 않은 주식 - 포트폴리오ID: {}, 주식코드: {}", portfolioId, stockCode);
                        return PortfolioStockException.portfolioStockNotFound();
                    });
            portfolioStocks.add(portfolioStock);
        }

        // 4. stockCode를 키로 하는 Map 생성 (O(1) 검색을 위해)
        Map<String, PortfolioStock> portfolioStockMap = portfolioStocks.stream()
                .collect(Collectors.toMap(PortfolioStock::getStockCode, Function.identity()));

        // 5. 각 PortfolioStock 업데이트
        List<PortfolioStockResponse> responses = new ArrayList<>();
        for (PortfolioStockUpdateRequest updateRequest : request.getStocks()) {
            PortfolioStock portfolioStock = portfolioStockMap.get(updateRequest.getStockCode());

            // 설정 업데이트
            portfolioStock.updateSettings(updateRequest.getTargetWeight(), updateRequest.getThresholdPercentage());

            // 응답 DTO 생성
            responses.add(PortfolioStockResponse.from(portfolioStock, null));

            log.debug("주식 설정 업데이트 완료 - 주식코드: {}, 목표비중: {}%, 임계값: {}%",
                    updateRequest.getStockCode(), updateRequest.getTargetWeight(), updateRequest.getThresholdPercentage());
        }

        log.info("포트폴리오 주식 일괄 업데이트 완료 - 사용자ID: {}, 포트폴리오ID: {}, 업데이트된 주식 수: {}",
                userId, portfolioId, responses.size());

        return responses;
    }
}