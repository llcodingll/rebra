package com.rebra.repository;

import com.rebra.entity.PortfolioStock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioStockRepository extends JpaRepository<PortfolioStock, Long> {

    /**
     * 포트폴리오별 활성 상태 주식 목록 조회
     */
    List<PortfolioStock> findByPortfolioIdAndStatus(Long portfolioId, String status);

    /**
     * 포트폴리오별 활성 상태 주식 개수 조회
     */
    long countByPortfolioIdAndStatus(Long portfolioId, String status);

    /**
     * 포트폴리오의 모든 주식 조회 (상태 무관)
     */
    List<PortfolioStock> findByPortfolioIdOrderByCreatedAtDesc(Long portfolioId);

    /**
     * 특정 포트폴리오의 특정 주식 조회
     */
    Optional<PortfolioStock> findByPortfolioIdAndStockCode(Long portfolioId, String stockCode);

    /**
     * 포트폴리오별 주식 존재 여부 확인
     */
    boolean existsByPortfolioIdAndStockCode(Long portfolioId, String stockCode);

    /**
     * 포트폴리오의 총 목표 비중 합계 조회
     */
    @Query("SELECT COALESCE(SUM(ps.targetWeight), 0) FROM PortfolioStock ps WHERE ps.portfolio.id = :portfolioId AND ps.status = :status")
    Double sumTargetWeightByPortfolioIdAndStatus(@Param("portfolioId") Long portfolioId, @Param("status") String status);

    /**
     * 특정 주식이 등록된 모든 포트폴리오 조회
     */
    List<PortfolioStock> findByStockCodeAndStatus(String stockCode, String status);

    /**
     * 사용자별 모든 포트폴리오 주식 조회 (JOIN)
     */
    @Query("SELECT ps FROM PortfolioStock ps JOIN ps.portfolio p WHERE p.user.id = :userId AND ps.status = :status")
    List<PortfolioStock> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 포트폴리오별 주식 목록 조회
     */
    List<PortfolioStock> findByPortfolioId(Long portfolioId);
}