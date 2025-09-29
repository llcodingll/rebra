package com.rebra.dummy.controller;

import com.rebra.common.CommonApiResponse;
import com.rebra.entity.Stock;
import com.rebra.entity.StockPrice;
import com.rebra.repository.StockRepository;
import com.rebra.repository.StockPriceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Tag(name = "Stock Data Import", description = "Stock 및 StockPrice 데이터 임포트 API")
public class StockDataImportController {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;

    @Operation(
            summary = "Stock 마스터 데이터 임포트",
            description = "data_5034_20250928.csv 파일에서 Stock 데이터를 읽어서 DB에 삽입합니다. " +
                    "종목코드는 6자리로 정규화되어 저장됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock 데이터 임포트 성공"),
            @ApiResponse(responseCode = "500", description = "임포트 중 오류 발생")
    })
    @PostMapping("/import-stocks")
    public ResponseEntity<CommonApiResponse<String>> importStocks() {
        try {
            log.info("Stock 마스터 데이터 임포트 시작");

            ClassPathResource resource = new ClassPathResource("data/data_5034_20250928.csv");
            List<Stock> stocks = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), Charset.forName("EUC-KR")))) {

                String line = reader.readLine(); // 헤더 스킵
                int lineNumber = 1;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;

                    try {
                        String[] columns = parseCsvLine(line);
                        if (columns.length >= 2) {
                            String stockCode = normalizeStockCode(columns[0].replace("\"", "").trim());
                            String stockName = columns[1].replace("\"", "").trim();

                            // 중복 체크
                            if (!stockRepository.existsByStockCode(stockCode)) {
                                Stock stock = Stock.builder()
                                        .stockCode(stockCode)
                                        .stockName(stockName)
                                        .stockType("주식")
                                        .isActive(true)
                                        .build();

                                stocks.add(stock);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("라인 {} 파싱 실패: {}", lineNumber, e.getMessage());
                    }

                    // 1000개씩 배치 저장
                    if (stocks.size() >= 1000) {
                        stockRepository.saveAll(stocks);
                        log.info("Stock {} 개 저장 완료", stocks.size());
                        stocks.clear();
                    }
                }

                // 마지막 배치 저장
                if (!stocks.isEmpty()) {
                    stockRepository.saveAll(stocks);
                    log.info("Stock {} 개 저장 완료", stocks.size());
                }
            }

            long totalCount = stockRepository.count();
            log.info("Stock 마스터 데이터 임포트 완료 - 총 {} 개", totalCount);

            return ResponseEntity.ok(CommonApiResponse.success(
                    String.format("Stock 마스터 데이터 임포트 완료 - 총 %d 개", totalCount)));

        } catch (Exception e) {
            log.error("Stock 데이터 임포트 실패", e);
            return ResponseEntity.internalServerError()
                    .body(CommonApiResponse.error("IMPORT_ERROR",
                            "Stock 데이터 임포트 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Operation(
            summary = "StockPrice 과거 데이터 임포트",
            description = "chunk_3~11.csv 파일들에서 StockPrice 데이터를 읽어서 DB에 삽입합니다. " +
                    "2021-09-03부터 2025-09-05까지의 데이터를 포함합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "StockPrice 데이터 임포트 성공"),
            @ApiResponse(responseCode = "500", description = "임포트 중 오류 발생")
    })
    @PostMapping("/import-stock-prices")
    public ResponseEntity<CommonApiResponse<String>> importStockPrices() {
        try {
            log.info("StockPrice 과거 데이터 임포트 시작");

            // Stock 엔티티들을 Map으로 캐싱
            Map<String, Stock> stockMap = stockRepository.findAll().stream()
                    .collect(Collectors.toMap(Stock::getStockCode, stock -> stock));

            int totalImported = 0;

            // chunk_3부터 chunk_11까지 처리
            for (int chunkNum = 3; chunkNum <= 11; chunkNum++) {
                String fileName = String.format("data/chunk_%d_KOSPI_*.csv", chunkNum);
                // 실제 파일명 찾기
                String actualFileName = findChunkFileName(chunkNum);
                if (actualFileName != null) {
                    int imported = importStockPricesFromFile(actualFileName, stockMap);
                    totalImported += imported;
                    log.info("파일 {} 처리 완료 - {} 개 데이터 임포트", actualFileName, imported);
                }
            }

            // Stock 데이터 범위 업데이트
            updateStockDataRanges();

            log.info("StockPrice 과거 데이터 임포트 완료 - 총 {} 개", totalImported);

            return ResponseEntity.ok(CommonApiResponse.success(
                    String.format("StockPrice 과거 데이터 임포트 완료 - 총 %d 개", totalImported)));

        } catch (Exception e) {
            log.error("StockPrice 데이터 임포트 실패", e);
            return ResponseEntity.internalServerError()
                    .body(CommonApiResponse.error("IMPORT_ERROR",
                            "StockPrice 데이터 임포트 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Operation(
            summary = "전체 데이터 임포트",
            description = "Stock과 StockPrice 데이터를 순차적으로 모두 임포트합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 데이터 임포트 성공"),
            @ApiResponse(responseCode = "500", description = "임포트 중 오류 발생")
    })
    @PostMapping("/import-all-data")
    public ResponseEntity<CommonApiResponse<String>> importAllData() {
        try {
            log.info("전체 데이터 임포트 시작");

            // 1. Stock 데이터 임포트
            ResponseEntity<CommonApiResponse<String>> stockResult = importStocks();
            if (!stockResult.getStatusCode().is2xxSuccessful()) {
                return stockResult;
            }

            // 2. StockPrice 데이터 임포트
            ResponseEntity<CommonApiResponse<String>> priceResult = importStockPrices();
            if (!priceResult.getStatusCode().is2xxSuccessful()) {
                return priceResult;
            }

            long stockCount = stockRepository.count();
            long priceCount = stockPriceRepository.count();

            log.info("전체 데이터 임포트 완료 - Stock: {} 개, StockPrice: {} 개", stockCount, priceCount);

            return ResponseEntity.ok(CommonApiResponse.success(
                    String.format("전체 데이터 임포트 완료 - Stock: %d 개, StockPrice: %d 개",
                            stockCount, priceCount)));

        } catch (Exception e) {
            log.error("전체 데이터 임포트 실패", e);
            return ResponseEntity.internalServerError()
                    .body(CommonApiResponse.error("IMPORT_ERROR",
                            "전체 데이터 임포트 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    private String normalizeStockCode(String stockCode) {
        if (stockCode.length() < 6) {
            return String.format("%6s", stockCode).replace(' ', '0');
        }
        return stockCode;
    }

    private String[] parseCsvLine(String line) {
        // 간단한 CSV 파싱 (쉼표로 분리)
        return line.split(",");
    }

    private String findChunkFileName(int chunkNum) {
        // chunk 파일명 매핑
        String[] chunkFiles = {
                "chunk_3_KOSPI_20210903_to_20220302.csv",
                "chunk_4_KOSPI_20220303_to_20220830.csv",
                "chunk_5_KOSPI_20220831_to_20230227.csv",
                "chunk_6_KOSPI_20230228_to_20230827.csv",
                "chunk_7_KOSPI_20230828_to_20240224.csv",
                "chunk_8_KOSPI_20240225_to_20240823.csv",
                "chunk_9_KOSPI_20240824_to_20250220.csv",
                "chunk_10_KOSPI_20250221_to_20250820.csv",
                "chunk_11_KOSPI_20250821_to_20250905.csv"
        };

        int index = chunkNum - 3;
        if (index >= 0 && index < chunkFiles.length) {
            return "data/" + chunkFiles[index];
        }
        return null;
    }

    private int importStockPricesFromFile(String fileName, Map<String, Stock> stockMap) throws Exception {
        ClassPathResource resource = new ClassPathResource(fileName);
        List<StockPrice> stockPrices = new ArrayList<>();
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // 헤더 스킵
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                try {
                    String[] columns = line.split(",");
                    if (columns.length >= 9) {
                        LocalDate date = LocalDate.parse(columns[0], DateTimeFormatter.ISO_LOCAL_DATE);
                        Integer openPrice = Integer.parseInt(columns[1]);
                        Integer highPrice = Integer.parseInt(columns[2]);
                        Integer lowPrice = Integer.parseInt(columns[3]);
                        Integer closePrice = Integer.parseInt(columns[4]);
                        Long volume = Long.parseLong(columns[5]);
                        Double changeRate = Double.parseDouble(columns[6]);
                        String ticker = normalizeStockCode(columns[7]);
                        String name = columns[8];

                        // Stock 엔티티 찾기
                        Stock stock = stockMap.get(ticker);
                        if (stock != null) {
                            StockPrice stockPrice = StockPrice.builder()
                                    .stock(stock)
                                    .ticker(ticker)
                                    .name(name)
                                    .date(date)
                                    .openPrice(openPrice)
                                    .highPrice(highPrice)
                                    .lowPrice(lowPrice)
                                    .closePrice(closePrice)
                                    .volume(volume)
                                    .changeRate(changeRate)
                                    .build();

                            stockPrices.add(stockPrice);
                        }
                    }
                } catch (Exception e) {
                    log.warn("파일 {} 라인 {} 파싱 실패: {}", fileName, lineNumber, e.getMessage());
                }

                // 1000개씩 배치 저장
                if (stockPrices.size() >= 1000) {
                    List<StockPrice> saved = stockPriceRepository.saveAllWithBatchCheck(stockPrices);
                    imported += saved.size();
                    stockPrices.clear();
                }
            }

            // 마지막 배치 저장
            if (!stockPrices.isEmpty()) {
                List<StockPrice> saved = stockPriceRepository.saveAllWithBatchCheck(stockPrices);
                imported += saved.size();
            }
        }

        return imported;
    }

    private void updateStockDataRanges() {
        LocalDate startDate = LocalDate.of(2021, 9, 3);
        LocalDate endDate = LocalDate.of(2025, 9, 5);

        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            stock.updateDataRange(startDate, endDate);
        }
        stockRepository.saveAll(stocks);

        log.info("Stock 데이터 범위 업데이트 완료 - {} ~ {}", startDate, endDate);
    }
}