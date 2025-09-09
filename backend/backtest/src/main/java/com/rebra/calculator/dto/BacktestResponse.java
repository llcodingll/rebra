package com.rebra.calculator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rebra.calculator.enums.BacktestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 백테스트 계산 결과를 나타내는 DTO 클래스
 * 계산 서버에서 메인 서버로 Kafka를 통해 전달되는 백테스트 결과를 담는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BacktestResponse {
    
    /**
     * 백테스트 기록 ID
     * 요청과 동일한 ID로 응답을 매칭
     */
    @JsonProperty("backtest_id")
    private Long backtestId;
    
    /**
     * 백테스트 계산 상태
     * COMPLETED, FAILED 등
     */
    @JsonProperty("status")
    private BacktestStatus status;
    
    /**
     * 백테스트 요약 결과
     * 전체적인 성과 지표들
     */
    @JsonProperty("summary")
    private BacktestSummaryDto summary;
    
    /**
     * 백테스트 상세 결과
     * 주기별 포트폴리오 가치와 수익률 기록
     */
    @JsonProperty("details")
    private List<BacktestDetailDto> details;
    
    
    
    /**
     * 오류 메시지
     * 계산 실패 시의 오류 내용
     */
    @JsonProperty("error_message")
    private String errorMessage;
    
    /**
     * 계산 소요 시간 (밀리초)
     * 성능 모니터링용
     */
    @JsonProperty("calculation_time_ms")
    private Long calculationTimeMs;

    /**
     * 성공적인 백테스트 결과를 생성하는 정적 팩토리 메서드
     * 
     * @param backtestId 백테스트 ID
     * @param summary 요약 결과
     * @param details 상세 결과
     * @param calculationTimeMs 계산 소요 시간
     * @return 성공 응답 객체
     */
    public static BacktestResponse success(Long backtestId, 
                                         BacktestSummaryDto summary,
                                         List<BacktestDetailDto> details,
                                         Long calculationTimeMs) {
        BacktestResponse response = new BacktestResponse();
        response.setBacktestId(backtestId);
        response.setStatus(BacktestStatus.COMPLETED);
        response.setSummary(summary);
        response.setDetails(details);
        response.setCalculationTimeMs(calculationTimeMs);
        return response;
    }

    /**
     * 실패한 백테스트 결과를 생성하는 정적 팩토리 메서드
     * 
     * @param backtestId 백테스트 ID
     * @param errorMessage 오류 메시지
     * @param calculationTimeMs 계산 소요 시간
     * @return 실패 응답 객체
     */
    public static BacktestResponse failure(Long backtestId, String errorMessage, Long calculationTimeMs) {
        BacktestResponse response = new BacktestResponse();
        response.setBacktestId(backtestId);
        response.setStatus(BacktestStatus.FAILED);
        response.setErrorMessage(errorMessage);
        response.setCalculationTimeMs(calculationTimeMs);
        return response;
    }

    /**
     * 백테스트가 성공적으로 완료되었는지 확인
     * 
     * @return 성공했으면 true
     */
    public boolean isSuccess() {
        return status == BacktestStatus.COMPLETED;
    }

    /**
     * 백테스트가 실패했는지 확인
     * 
     * @return 실패했으면 true
     */
    public boolean isFailure() {
        return status == BacktestStatus.FAILED;
    }

    /**
     * 응답에 유효한 데이터가 포함되어 있는지 확인
     * 
     * @return 유효한 응답이면 true
     */
    public boolean hasValidData() {
        if (!isSuccess()) {
            return false;
        }
        
        if (summary == null) {
            return false;
        }
        
        if (details == null || details.isEmpty()) {
            return false;
        }
        
        return true;
    }





    /**
     * 백테스트 기간을 일 단위로 반환
     * 상세 기록의 첫날과 마지막 날을 기준으로 계산
     * 
     * @return 백테스트 기간 (일)
     */
    public int getBacktestPeriodDays() {
        if (details == null || details.isEmpty()) {
            return 0;
        }
        
        // 상세 기록이 날짜순으로 정렬되어 있다고 가정
        return details.size();
    }

    /**
     * 평균 일일 수익률을 계산
     * 
     * @return 평균 일일 수익률
     */
    public double getAverageDailyReturn() {
        if (summary == null || summary.getTotalReturn() == null) {
            return 0.0;
        }
        
        int days = getBacktestPeriodDays();
        if (days <= 1) {
            return 0.0;
        }
        
        // 복리 수익률을 일일 수익률로 변환
        double totalReturn = summary.getTotalReturn();
        return Math.pow(1 + totalReturn, 1.0 / days) - 1;
    }

    /**
     * 연환산 수익률을 계산
     * 
     * @return 연환산 수익률
     */
    public double getAnnualizedReturn() {
        double dailyReturn = getAverageDailyReturn();
        return Math.pow(1 + dailyReturn, 365) - 1;
    }

    /**
     * 백테스트 결과 요약을 문자열로 반환
     * 
     * @return 결과 요약
     */
    public String getSummaryString() {
        if (!isSuccess() || summary == null) {
            return String.format("백테스트 ID: %d - %s", backtestId, 
                    isFailure() ? "실패: " + errorMessage : "데이터 없음");
        }
        
        return String.format("백테스트 ID: %d - 총 수익률: %.2f%%, 초과 수익률: %.2f%%, 리밸런싱: %d회, 소요시간: %dms",
                backtestId,
                summary.getTotalReturn() * 100,
                summary.getExcessReturn() * 100,
                summary.getRebalancingCount(),
                calculationTimeMs);
    }

    /**
     * 상세한 백테스트 결과를 문자열로 반환
     * 
     * @return 상세 결과
     */
    public String getDetailedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("백테스트 계산 결과\n");
        sb.append("==================\n");
        sb.append(String.format("백테스트 ID: %d\n", backtestId));
        sb.append(String.format("상태: %s\n", status));
        
        if (isSuccess() && summary != null) {
            sb.append(String.format("최종 가치: %,.0f원\n", summary.getFinalValue()));
            sb.append(String.format("총 수익률: %.2f%%\n", summary.getTotalReturn() * 100));
            sb.append(String.format("바이앤홀드 수익률: %.2f%%\n", summary.getBuyHoldReturn() * 100));
            sb.append(String.format("초과 수익률: %.2f%%\n", summary.getExcessReturn() * 100));
            sb.append(String.format("총 거래비용: %,.0f원\n", summary.getTotalFee()));
            sb.append(String.format("총 차입비용: %,.0f원\n", summary.getTotalBorrowingCost()));
            sb.append(String.format("리밸런싱 횟수: %d회\n", summary.getRebalancingCount()));
            sb.append(String.format("승률: %.1f%%\n", summary.getWinRate() * 100));
            sb.append(String.format("백테스트 기간: %d일\n", getBacktestPeriodDays()));
            
            if (summary.getTotalBorrowingCost() != null && summary.getTotalBorrowingCost() > 0) {
                sb.append(String.format("최대 차입금: %,.0f원\n", summary.getMaxBorrowingAmount()));
            }
        } else if (isFailure()) {
            sb.append(String.format("오류 메시지: %s\n", errorMessage));
        }
        
        sb.append(String.format("계산 소요 시간: %dms\n", calculationTimeMs));
        
        return sb.toString();
    }

    /**
     * 응답 데이터의 유효성을 검증
     * 
     * @return 유효한 응답이면 true
     */
    public boolean isValid() {
        try {
            if (backtestId == null || backtestId <= 0) {
                return false;
            }
            
            if (status == null) {
                return false;
            }
            
            if (isSuccess()) {
                // 성공인 경우 필수 데이터 검증
                if (summary == null || !summary.isValid()) {
                    return false;
                }
                
                if (details == null || details.isEmpty()) {
                    return false;
                }
                
                // 모든 상세 기록이 유효한지 검증
                for (BacktestDetailDto detail : details) {
                    if (detail == null || !detail.isValid()) {
                        return false;
                    }
                }
            } else if (isFailure()) {
                // 실패인 경우 오류 메시지 필수
                if (errorMessage == null || errorMessage.trim().isEmpty()) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BacktestResponse that = (BacktestResponse) obj;
        return java.util.Objects.equals(backtestId, that.backtestId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(backtestId);
    }
}