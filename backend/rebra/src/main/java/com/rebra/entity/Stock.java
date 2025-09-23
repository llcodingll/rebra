package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @SequenceGenerator(
        name = "stock_seq_generator",
        sequenceName = "stock_seq",
        allocationSize = 10
    )
    @Column(name = "stock_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_seq_generator")
    private Long id;

    @Column(name = "stock_code", nullable = false, unique = true)
    private String stockCode;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "stock_type", nullable = false)
    private String stockType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "data_start_date")
    private LocalDate dataStartDate;

    @Column(name = "data_end_date")
    private LocalDate dataEndDate;

    @Column(name = "last_data_sync")
    private LocalDateTime lastDataSync;

    @OneToMany(mappedBy = "stock", fetch = FetchType.LAZY)
    private List<StockPrice> stockPrices = new ArrayList<>();

    @Builder
    public Stock(String stockCode, String stockName, String stockType, Boolean isActive) {
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.stockType = stockType;
        this.isActive = isActive;
    }

    // 기존 생성자와 호환성을 위한 생성자 (Lombok @AllArgsConstructor 역할)
    public Stock(String stockCode, String stockName, String stockType, Boolean isActive,
                 LocalDate dataStartDate, LocalDate dataEndDate, LocalDateTime lastDataSync) {
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.stockType = stockType;
        this.isActive = isActive;
        this.dataStartDate = dataStartDate;
        this.dataEndDate = dataEndDate;
        this.lastDataSync = lastDataSync;
    }

    public void updateDataRange(LocalDate start, LocalDate end) {
        if (this.dataStartDate == null || (start != null && start.isBefore(this.dataStartDate))) {
            this.dataStartDate = start;
        }
        if (this.dataEndDate == null || (end != null && end.isAfter(this.dataEndDate))) {
            this.dataEndDate = end;
        }
        this.lastDataSync = LocalDateTime.now();
    }


    public boolean hasDataInRange(LocalDate start, LocalDate end) {
        return dataStartDate != null && dataEndDate != null &&
               !start.isBefore(dataStartDate) && !end.isAfter(dataEndDate);
    }
}