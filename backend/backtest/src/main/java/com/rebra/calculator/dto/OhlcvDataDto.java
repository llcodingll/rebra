package com.rebra.calculator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 백테스트 계산용 주가 데이터 DTO 클래스
 * 종가 기준 백테스트에 필요한 최소한의 정보만 포함한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OhlcvDataDto {
    
    /**
     * 거래 날짜
     * 해당 OHLCV 데이터의 거래일자
     */
    @JsonProperty("trade_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tradeDate;
    
    /**
     * 종목 코드
     * 한국거래소 6자리 종목코드 (예: "005930")
     */
    @JsonProperty("stock_code")
    private String stockCode;
    
    /**
     * 종가 (원)
     * 해당일 시장 마감 시 마지막 거래 가격
     * 백테스트 계산에서 가장 중요한 가격
     */
    @JsonProperty("close_price")
    private Double closePrice;
    


    /**
     * 주가 데이터의 유효성을 검증한다
     * 
     * @return 유효한 데이터면 true
     */
    public boolean isValid() {
        try {
            // 거래 날짜 검증
            if (tradeDate == null) {
                return false;
            }
            
            // 종목 코드 검증
            if (stockCode == null || stockCode.trim().isEmpty()) {
                return false;
            }
            
            // 종목 코드 형식 검증 (6자리 숫자)
            if (!stockCode.trim().matches("\\d{6}")) {
                return false;
            }
            
            // 종가 검증 (필수, 양수)
            if (closePrice == null || closePrice <= 0) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 종목 코드를 정규화하여 반환
     * 
     * @return 정규화된 종목 코드 (대문자, 공백 제거)
     */
    public String getNormalizedStockCode() {
        return stockCode != null ? stockCode.trim().toUpperCase() : null;
    }

    /**
     * 안전하게 종가를 반환
     * null인 경우 0.0을 반환
     * 
     * @return 종가
     */
    public double getSafeClosePrice() {
        return closePrice != null ? closePrice : 0.0;
    }


    /**
     * 간단한 정보를 문자열로 반환
     * 
     * @return 간단한 정보 (날짜, 종목, 종가)
     */
    public String getSimpleInfo() {
        return String.format("%s %s: %.0f원", tradeDate, stockCode, getSafeClosePrice());
    }

    /**
     * 상세한 주가 정보를 문자열로 반환
     * 
     * @return 상세 정보
     */
    public String getDetailedInfo() {
        return String.format("%s %s: %.0f원", tradeDate, stockCode, getSafeClosePrice());
    }

    /**
     * 두 주가 데이터가 같은 종목의 같은 날짜인지 확인
     * 
     * @param other 비교할 다른 주가 데이터
     * @return 같은 종목의 같은 날짜면 true
     */
    public boolean isSameStockAndDate(OhlcvDataDto other) {
        if (other == null) {
            return false;
        }
        
        return Objects.equals(tradeDate, other.tradeDate) &&
               Objects.equals(getNormalizedStockCode(), other.getNormalizedStockCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        OhlcvDataDto that = (OhlcvDataDto) obj;
        return Objects.equals(tradeDate, that.tradeDate) &&
               Objects.equals(getNormalizedStockCode(), that.getNormalizedStockCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeDate, getNormalizedStockCode());
    }
}