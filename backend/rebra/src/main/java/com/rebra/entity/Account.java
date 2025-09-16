package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account")
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "broker_name", nullable = false)
    private String brokerName;

    @Column(name = "account_number", nullable = false, columnDefinition = "TEXT", updatable = false)
    private String accountNumber;  // 암호화된 계좌번호

    @Column(name = "account_number_hash", nullable = false, unique = true, length = 64, updatable = false)
    private String accountNumberHash;  // 중복 확인용 해시

    @Column(name = "app_key", nullable = false, columnDefinition = "TEXT")
    private String appKey;  // 암호화된 앱키

    @Column(name = "app_secret", nullable = false, columnDefinition = "TEXT")
    private String appSecret;  // 암호화된 앱시크릿

    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "is_connected")
    private boolean isConnected = true;

    @Builder
    public Account(User user, String accountNumber, String accountNumberHash,
                   String appKey, String appSecret, String brokerName, AccountType accountType,
                   boolean isConnected) {
        this.user = user;
        this.accountNumber = accountNumber;
        this.accountNumberHash = accountNumberHash;
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.brokerName = brokerName;
        this.accountType = accountType;
        this.isConnected = isConnected;
    }


    /**
     * 연결 상태 업데이트
     */
    public void updateIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

}