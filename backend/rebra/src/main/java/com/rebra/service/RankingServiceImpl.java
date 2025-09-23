package com.rebra.service;

import com.rebra.component.KisApiComponent;
import com.rebra.dto.response.FluctuationRankingResponse;
import com.rebra.dto.response.VolumeRankingResponse;
import com.rebra.entity.Account;
import com.rebra.repository.AccountRepository;
import com.rebra.component.kisApi.FluctuationRankingResult;
import com.rebra.component.kisApi.VolumeRankResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final KisApiComponent kisApiComponent;
    private final AccountRepository accountRepository;

    @Override
    public VolumeRankingResponse getVolumeRanking(Long accountId) {
        log.info("거래량 순위 조회 시작 - accountId: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        VolumeRankResult result = kisApiComponent.getVolumeRanking(account);

        log.info("거래량 순위 조회 완료 - accountId: {}", accountId);
        return VolumeRankingResponse.from(result);
    }

    @Override
    public FluctuationRankingResponse getFluctuationRankingRising(Long accountId) {
        log.info("급상승 종목 순위 조회 시작 - accountId: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        FluctuationRankingResult result = kisApiComponent.getFluctuationRankingRising(account);

        log.info("급상승 종목 순위 조회 완료 - accountId: {}", accountId);
        return FluctuationRankingResponse.fromRising(result);
    }

    @Override
    public FluctuationRankingResponse getFluctuationRankingFalling(Long accountId) {
        log.info("급하락 종목 순위 조회 시작 - accountId: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        FluctuationRankingResult result = kisApiComponent.getFluctuationRankingFalling(account);

        log.info("급하락 종목 순위 조회 완료 - accountId: {}", accountId);
        return FluctuationRankingResponse.fromFalling(result);
    }
}