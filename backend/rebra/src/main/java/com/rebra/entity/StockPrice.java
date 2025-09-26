package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stock_prices",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ticker", "date"}),
    indexes = {
        @Index(name = "idx_stock_ticker_date", columnList = "ticker, date"),
        @Index(name = "idx_stock_id_date", columnList = "stock_id, date")
    })
public class StockPrice extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "stock_price_seq_generator",
        sequenceName = "stock_price_seq",
        allocationSize = 100
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_price_seq_generator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer openPrice;

    @Column(nullable = false)
    private Integer highPrice;

    @Column(nullable = false)
    private Integer lowPrice;

    @Column(nullable = false)
    private Integer closePrice;

    @Column(nullable = false)
    private Long volume;

    @Column(nullable = false)
    private Double changeRate;

}