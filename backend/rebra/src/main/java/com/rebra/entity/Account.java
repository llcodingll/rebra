package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "broker_name", nullable = false)
    private String brokerName;

    @Column(name = "app_key", nullable = false)
    private String appKey;

    @Column(name = "app_secret", nullable = false)
    private String appSecret;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    public Account(User user, String accountNumber, String brokerName, 
                   String appKey, String appSecret, Boolean isActive) {
        this.user = user;
        this.accountNumber = accountNumber;
        this.brokerName = brokerName;
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.isActive = isActive;
    }
}