package com.rebra.repository;

import com.rebra.entity.Watchlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    @Query("SELECT w FROM Watchlist w WHERE w.portfolio.id = :portfolioId AND w.stock.stockCode = :stockCode")
    Optional<Watchlist> findByPortfolioIdAndStockCode(@Param("portfolioId") Long portfolioId, @Param("stockCode") String stockCode);

    @Query("SELECT w FROM Watchlist w JOIN FETCH w.stock WHERE w.portfolio.id = :portfolioId ORDER BY w.createdAt DESC")
    List<Watchlist> findByPortfolioIdWithStock(@Param("portfolioId") Long portfolioId);

    @Modifying
    @Query("DELETE FROM Watchlist w WHERE w.portfolio.id = :portfolioId AND w.stock.stockCode = :stockCode")
    void deleteByPortfolioIdAndStockCode(@Param("portfolioId") Long portfolioId, @Param("stockCode") String stockCode);

    @Query("SELECT COUNT(w) > 0 FROM Watchlist w WHERE w.portfolio.id = :portfolioId AND w.stock.stockCode = :stockCode")
    boolean existsByPortfolioIdAndStockCode(@Param("portfolioId") Long portfolioId, @Param("stockCode") String stockCode);

}
