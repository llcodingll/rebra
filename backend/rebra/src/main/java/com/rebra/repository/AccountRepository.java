package com.rebra.repository;

import com.rebra.entity.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * 사용자별 활성 계좌 목록 조회
     */
    List<Account> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    /**
     * 삭제되지 않은 특정 계좌 조회
     */
    boolean existsByAccountNumberHashAndIsDeletedFalse(String accountNumberHash);

    /**
     * 사용자별 모든 계좌 목록 조회 (비활성 포함)
     */
    List<Account> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 특정 계좌 조회 (소유자 검증 포함)
     */
    Optional<Account> findByIdAndUserId(Long id, Long userId);

    /**
     * 사용자의 활성 계좌 조회 (소유자 검증 포함)
     */
    Optional<Account> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

    /**
     * 사용자별 활성 계좌 개수 조회
     */
    long countByUserIdAndIsDeletedFalse(Long userId);

    /**
     * 계좌번호 중복 확인 (동일 사용자 내에서) 암호화된 계좌번호로 저장되므로 복호화 후 비교는 서비스 레이어에서 처리
     */
    List<Account> findByUserIdAndIsDeletedFalse(Long userId);

    /**
     * 브로커별 계좌 개수 조회
     */
    long countByUserIdAndBrokerNameAndIsDeletedFalse(Long userId, String brokerName);

    /**
     * 사용자의 첫 번째 활성 계좌 조회 (기본 계좌로 사용)
     */
    Optional<Account> findTopByUserIdAndIsDeletedFalseOrderByCreatedAtAsc(Long userId);

    /**
     * 계좌번호 해시 기반 중복 확인
     */
    boolean existsByAccountNumberHash(String accountNumberHash);

    /**
     * 계좌번호 해시로 계좌 조회
     */
    Optional<Account> findByAccountNumberHash(String accountNumberHash);
}