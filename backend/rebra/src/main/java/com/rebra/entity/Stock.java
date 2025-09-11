package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stock",
    indexes = {
        @Index(name = "idx_stock_name", columnList = "stock_name"),
        @Index(name = "idx_stock_code", columnList = "stock_code")
    })
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", nullable = false, unique = true)
    private String stockCode;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "stock_type", nullable = false)
    private String stockType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    public Stock(String stockCode, String stockName, String stockType, Boolean isActive) {
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.stockType = stockType;
        this.isActive = isActive;
    }
}