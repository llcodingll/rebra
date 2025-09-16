package com.rebra.repository;

import com.rebra.entity.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * 사용자별 계좌 목록 조회
     */
    List<Account> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 계좌번호 해시로 계좌 존재 여부 확인
     */
    boolean existsByAccountNumberHash(String accountNumberHash);

    /**
     * 사용자의 특정 계좌 조회 (소유자 검증 포함)
     */
    Optional<Account> findByIdAndUserId(Long id, Long userId);

    /**
     * 사용자별 계좌 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 사용자별 계좌 목록 조회
     */
    List<Account> findByUserId(Long userId);

    /**
     * 브로커별 계좌 개수 조회
     */
    long countByUserIdAndBrokerName(Long userId, String brokerName);

    /**
     * 사용자의 첫 번째 계좌 조회 (기본 계좌로 사용)
     */
    Optional<Account> findTopByUserIdOrderByCreatedAtAsc(Long userId);

    /**
     * 사용자의 첫 번째 활성 계좌 조회 (연결된 계좌만)
     */
    Optional<Account> findTopByUserIdAndIsConnectedOrderByCreatedAtAsc(Long userId, boolean isConnected);


    /**
     * 계좌번호 해시로 계좌 조회
     */
    Optional<Account> findByAccountNumberHash(String accountNumberHash);

    /**
     * Portfolio와 연관되지 않은 사용자의 활성 계좌 목록 조회
     * 추후 QueryDSL 도입 예정
     */
    @Query(value = """
        SELECT * FROM account a
        WHERE a.user_id = :userId
          AND NOT EXISTS (
              SELECT 1 FROM portfolio p
              WHERE p.account_id = a.id
          )
        ORDER BY a.created_at DESC
        """, nativeQuery = true)
    List<Account> findAvailableAccountsByUserId(@Param("userId") Long userId);
}