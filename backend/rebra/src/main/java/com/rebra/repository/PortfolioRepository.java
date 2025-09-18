package com.rebra.repository;

import com.rebra.entity.Portfolio;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * 사용자별 포트폴리오 목록 조회
     */
    List<Portfolio> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 계좌의 포트폴리오 조회
     */
    Optional<Portfolio> findByAccountId(Long accountId);

    /**
     * 사용자의 특정 포트폴리오 조회 (소유자 검증 포함)
     */
    Optional<Portfolio> findByIdAndUserId(Long id, Long userId);

    /**
     * 사용자별 포트폴리오 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 자동 리밸런싱이 활성화된 포트폴리오 목록 조회
     */
    List<Portfolio> findByAutoRebalancingTrueOrderByNextRebalanceDateAsc();

    /**
     * 사용자별 자동 리밸런싱 활성화된 포트폴리오 개수
     */
    long countByUserIdAndAutoRebalancingTrue(Long userId);

    /**
     * 계좌 ID로 포트폴리오 존재 여부 확인
     */
    boolean existsByAccountId(Long accountId);

    /**
     * 사용자의 최근 생성된 포트폴리오 조회
     */
    Optional<Portfolio> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 리밸런싱 전략별 포트폴리오 조회
     */
    @Query("SELECT p FROM Portfolio p WHERE p.user.id = :userId AND p.rebalancingStrategy = :strategy ORDER BY p.createdAt DESC")
    List<Portfolio> findByUserIdAndRebalancingStrategy(@Param("userId") Long userId, @Param("strategy") String strategy);

    /**
     * 계좌 ID로 포트폴리오 삭제
     */
    void deleteByAccountId(Long accountId);

    /**
     * portfolioStocks와 함께 포트폴리오 조회 (fetch join)
     */
    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.portfolioStocks WHERE p.id = :id")
    Optional<Portfolio> findByIdWithPortfolioStocks(@Param("id") Long id);

    /**
     * 사용자 검증과 함께 portfolioStocks를 포함한 포트폴리오 조회 (fetch join)
     */
    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.portfolioStocks WHERE p.id = :id AND p.user.id = :userId")
    Optional<Portfolio> findByIdAndUserIdWithPortfolioStocks(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 특정 포트폴리오의 생성일시 업데이트
     */
    @Modifying
    @Query("UPDATE Portfolio p SET p.createdAt = :createdAt WHERE p.id = :portfolioId")
    int updateCreatedAtById(@Param("portfolioId") Long portfolioId, @Param("createdAt") LocalDateTime createdAt);
}