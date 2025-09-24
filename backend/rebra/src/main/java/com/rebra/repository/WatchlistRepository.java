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

    @Query("SELECT w FROM Watchlist w WHERE w.user.id = :userId AND w.stock.stockCode = :stockCode")
    Optional<Watchlist> findByUserIdAndStockCode(@Param("userId") Long userId, @Param("stockCode") String stockCode);

    @Query("SELECT w FROM Watchlist w JOIN FETCH w.stock WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    List<Watchlist> findByUserIdWithStock(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Watchlist w WHERE w.user.id = :userId AND w.stock.stockCode = :stockCode")
    void deleteByUserIdAndStockCode(@Param("userId") Long userId, @Param("stockCode") String stockCode);

    @Query("SELECT COUNT(w) > 0 FROM Watchlist w WHERE w.user.id = :userId AND w.stock.stockCode = :stockCode")
    boolean existsByUserIdAndStockCode(@Param("userId") Long userId, @Param("stockCode") String stockCode);

}
