package com.rebra.util;

import com.rebra.dto.realtime.OptimizedOrderbookData;
import com.rebra.dto.realtime.OptimizedPriceData;
import com.youhogeon.finance.kis_api.api.realtime.H0STASP0Data;
import com.youhogeon.finance.kis_api.api.realtime.H0STCNT0Data;
import com.youhogeon.finance.kis_api.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RealtimeDataTransformer {

    public static OptimizedOrderbookData transformOrderbookData(H0STASP0Data kisData, String stockCode) {
        if (kisData == null) {
            log.warn("KIS 호가 데이터가 null입니다 - StockCode: {}", stockCode);
            return null;
        }

        try {
            // KIS 라이브러리 JsonUtil을 활용해서 데이터 검증
            String jsonData = JsonUtil.toJson(kisData);
            if (jsonData == null || jsonData.trim().isEmpty() || "null".equals(jsonData)) {
                log.warn("호가 데이터 JSON 변환 실패 또는 비어있음 - StockCode: {}", stockCode);
                return null;
            }

            log.info("📊 호가 데이터 변환 시작 - StockCode: {}, DataType: {}, JsonSize: {}bytes",
                     stockCode, kisData.getClass().getSimpleName(), jsonData.length());

            // 핵심 필드 유효성 검증
            if (!validateOrderbookData(kisData, stockCode)) {
                return null;
            }

            OptimizedOrderbookData optimizedData = OptimizedOrderbookData.builder()
                    .stockCode(stockCode)
                    .askp1(safeGetString(kisData::getAskp1))
                    .askp2(safeGetString(kisData::getAskp2))
                    .askp3(safeGetString(kisData::getAskp3))
                    .askp4(safeGetString(kisData::getAskp4))
                    .askp5(safeGetString(kisData::getAskp5))
                    .askp6(safeGetString(kisData::getAskp6))
                    .askp7(safeGetString(kisData::getAskp7))
                    .askp8(safeGetString(kisData::getAskp8))
                    .askp9(safeGetString(kisData::getAskp9))
                    .askp10(safeGetString(kisData::getAskp10))
                    .bidp1(safeGetString(kisData::getBidp1))
                    .bidp2(safeGetString(kisData::getBidp2))
                    .bidp3(safeGetString(kisData::getBidp3))
                    .bidp4(safeGetString(kisData::getBidp4))
                    .bidp5(safeGetString(kisData::getBidp5))
                    .bidp6(safeGetString(kisData::getBidp6))
                    .bidp7(safeGetString(kisData::getBidp7))
                    .bidp8(safeGetString(kisData::getBidp8))
                    .bidp9(safeGetString(kisData::getBidp9))
                    .bidp10(safeGetString(kisData::getBidp10))
                    .askpRsqn1(safeGetString(kisData::getAskpRsqn1))
                    .askpRsqn2(safeGetString(kisData::getAskpRsqn2))
                    .askpRsqn3(safeGetString(kisData::getAskpRsqn3))
                    .askpRsqn4(safeGetString(kisData::getAskpRsqn4))
                    .askpRsqn5(safeGetString(kisData::getAskpRsqn5))
                    .askpRsqn6(safeGetString(kisData::getAskpRsqn6))
                    .askpRsqn7(safeGetString(kisData::getAskpRsqn7))
                    .askpRsqn8(safeGetString(kisData::getAskpRsqn8))
                    .askpRsqn9(safeGetString(kisData::getAskpRsqn9))
                    .askpRsqn10(safeGetString(kisData::getAskpRsqn10))
                    .bidpRsqn1(safeGetString(kisData::getBidpRsqn1))
                    .bidpRsqn2(safeGetString(kisData::getBidpRsqn2))
                    .bidpRsqn3(safeGetString(kisData::getBidpRsqn3))
                    .bidpRsqn4(safeGetString(kisData::getBidpRsqn4))
                    .bidpRsqn5(safeGetString(kisData::getBidpRsqn5))
                    .bidpRsqn6(safeGetString(kisData::getBidpRsqn6))
                    .bidpRsqn7(safeGetString(kisData::getBidpRsqn7))
                    .bidpRsqn8(safeGetString(kisData::getBidpRsqn8))
                    .bidpRsqn9(safeGetString(kisData::getBidpRsqn9))
                    .bidpRsqn10(safeGetString(kisData::getBidpRsqn10))
                    .totalAskpRsqn(safeGetString(kisData::getTotalAskpRsqn))
                    .totalBidpRsqn(safeGetString(kisData::getTotalBidpRsqn))
                    .timestamp(System.currentTimeMillis())
                    .build();

            log.info("✅ 호가 데이터 변환 완료 - StockCode: {}, Ask1: {}, Bid1: {}",
                     stockCode, optimizedData.getAskp1(), optimizedData.getBidp1());
            return optimizedData;

        } catch (Exception e) {
            log.error("호가 데이터 변환 중 오류 발생 - StockCode: {}", stockCode, e);
            return null;
        }
    }

    public static OptimizedPriceData transformPriceData(H0STCNT0Data kisData, String stockCode) {
        if (kisData == null) {
            log.warn("KIS 체결 데이터가 null입니다 - StockCode: {}", stockCode);
            return null;
        }

        try {
            // KIS 라이브러리 JsonUtil을 활용해서 데이터 검증
            String jsonData = JsonUtil.toJson(kisData);
            if (jsonData == null || jsonData.trim().isEmpty() || "null".equals(jsonData)) {
                log.warn("체결 데이터 JSON 변환 실패 또는 비어있음 - StockCode: {}", stockCode);
                return null;
            }

            log.info("📈 체결 데이터 변환 시작 - StockCode: {}, DataType: {}, JsonSize: {}bytes",
                     stockCode, kisData.getClass().getSimpleName(), jsonData.length());

            // 핵심 필드 유효성 검증
            if (!validatePriceData(kisData, stockCode)) {
                return null;
            }

            OptimizedPriceData optimizedData = OptimizedPriceData.builder()
                    .stockCode(stockCode)
                    .stckPrpr(safeGetString(kisData::getStckPrpr))
                    .prdyVrssSign(safeGetString(kisData::getPrdyVrssSign))
                    .prdyVrss(safeGetString(kisData::getPrdyVrss))
                    .prdyCtrt(safeGetString(kisData::getPrdyCtrt))
                    .stckOprc(safeGetString(kisData::getStckOprc))
                    .stckHgpr(safeGetString(kisData::getStckHgpr))
                    .stckLwpr(safeGetString(kisData::getStckLwpr))
                    .cntgVol(safeGetString(kisData::getCntgVol))
                    .acmlVol(safeGetString(kisData::getAcmlVol))
                    .acmlTrPbmn(safeGetString(kisData::getAcmlTrPbmn))
                    .askp1(safeGetString(kisData::getAskp1))
                    .bidp1(safeGetString(kisData::getBidp1))
                    .askpRsqn1(safeGetString(kisData::getAskpRsqn1))
                    .bidpRsqn1(safeGetString(kisData::getBidpRsqn1))
                    .timestamp(System.currentTimeMillis())
                    .build();

            log.info("✅ 체결 데이터 변환 완료 - StockCode: {}, Price: {}, Change: {}",
                     stockCode, optimizedData.getStckPrpr(), optimizedData.getPrdyVrss());
            return optimizedData;

        } catch (Exception e) {
            log.error("체결 데이터 변환 중 오류 발생 - StockCode: {}", stockCode, e);
            return null;
        }
    }

    private static String safeGetString(Supplier<Object> supplier) {
        try {
            Object value = supplier.get();
            if (value == null) {
                return "0";
            }
            return value.toString();
        } catch (Exception e) {
            log.warn("필드 접근 중 오류 발생, 기본값 사용: {}", e.getMessage());
            return "0";
        }
    }

    /**
     * 호가 데이터 핵심 필드 유효성 검증
     */
    private static boolean validateOrderbookData(H0STASP0Data kisData, String stockCode) {
        try {
            // 1차 매도/매수 호가가 있는지 확인 (가장 중요한 데이터)
            Object askp1 = kisData.getAskp1();
            Object bidp1 = kisData.getBidp1();

            if (askp1 == null && bidp1 == null) {
                log.warn("호가 데이터 유효성 검증 실패 - 1차 매도/매수 호가 모두 null - StockCode: {}", stockCode);
                return false;
            }

            log.info("✅ 호가 데이터 유효성 검증 통과 - StockCode: {}, Ask1: {}, Bid1: {}",
                     stockCode, askp1, bidp1);
            return true;

        } catch (Exception e) {
            log.warn("호가 데이터 유효성 검증 중 오류 - StockCode: {}, Error: {}", stockCode, e.getMessage());
            return false;
        }
    }

    /**
     * 체결가 데이터 핵심 필드 유효성 검증
     */
    private static boolean validatePriceData(H0STCNT0Data kisData, String stockCode) {
        try {
            // 현재가가 있는지 확인 (가장 중요한 데이터)
            Object stckPrpr = kisData.getStckPrpr();

            if (stckPrpr == null) {
                log.warn("체결 데이터 유효성 검증 실패 - 현재가 null - StockCode: {}", stockCode);
                return false;
            }

            log.info("✅ 체결 데이터 유효성 검증 통과 - StockCode: {}, Price: {}", stockCode, stckPrpr);
            return true;

        } catch (Exception e) {
            log.warn("체결 데이터 유효성 검증 중 오류 - StockCode: {}, Error: {}", stockCode, e.getMessage());
            return false;
        }
    }

    @FunctionalInterface
    private interface Supplier<T> {
        T get() throws Exception;
    }
}