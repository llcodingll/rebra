package com.rebra.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {

    // ==== User 관련 ====
    USER_NOT_FOUND(6001, "사용자를 찾을수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS(6003, "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(6004, "접근이 거부되었습니다.", HttpStatus.FORBIDDEN),
    INVALID_USER_FORMAT(6005, "잘못된 사용자 ID 형식입니다.", HttpStatus.BAD_REQUEST),
    NOT_LOGGED_IN(6006, "로그인이 필요합니다.", HttpStatus.BAD_REQUEST),
    NICKNAME_ALREADY_EXISTS(6007, "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),

    // ==== OAuth2 관련 ====
    MISSING_AUTHORIZATION_CODE(7001, "인가코드가 필요합니다.", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_PRINCIPAL_TYPE(7002, "지원하지 않는 principal 타입입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ID_TOKEN(7003, "ID 토큰 검증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    KAKAO_TOKEN_FETCH_FAILED(7004, "카카오 토큰 발급에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    KAKAO_JWKS_FETCH_FAILED(7005, "카카오 공개키 조회에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    ID_TOKEN_SUB_EXTRACTION_FAILED(7006, "ID 토큰에서 사용자 정보 추출에 실패했습니다.", HttpStatus.BAD_REQUEST),
    ID_TOKEN_SIGNATURE_INVALID(7007, "ID 토큰 서명이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    ID_TOKEN_PROCESSING_FAILED(7008, "ID 토큰 처리에 실패했습니다.", HttpStatus.BAD_REQUEST),

    // ==== JWT/Token 관련 ====
    INVALID_TEMP_TOKEN(8001, "유효하지 않은 임시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TEMP_TOKEN(8002, "만료된 임시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_TYPE(8003, "잘못된 토큰 타입입니다.", HttpStatus.BAD_REQUEST),
    TEMP_TOKEN_PARSING_FAILED(8004, "임시 토큰 파싱에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(8005, "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND(8007, "리프레시 토큰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_EXPIRED(8008, "리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_GENERATION_FAILED(8009, "토큰 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==== Backtest 관련 ====
    BACKTEST_NOT_FOUND(9001, "백테스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BACKTEST_ACCESS_DENIED(9002, "백테스트 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    BACKTEST_INVALID_REQUEST(9003, "잘못된 백테스트 요청입니다.", HttpStatus.BAD_REQUEST),
    BACKTEST_PROCESSING(9004, "백테스트가 아직 처리 중입니다.", HttpStatus.ACCEPTED),
    BACKTEST_FAILED(9005, "백테스트 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    BACKTEST_DELETE_NOT_ALLOWED(9006, "진행 중인 백테스트는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // ==== Stock 관련 ====
    STOCK_NOT_FOUND(10001, "주식을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    STOCK_CODE_NOT_FOUND(10002, "해당 종목 코드로 주식을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    STOCK_NAME_NOT_FOUND(10003, "해당 종목명으로 주식을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // ==== Account 관련 ====
    ACCOUNT_NOT_FOUND(11001, "계좌를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ACCOUNT_ACCESS_DENIED(11002, "계좌 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    DUPLICATE_ACCOUNT(11003, "이미 등록된 계좌입니다.", HttpStatus.CONFLICT),
    ACCOUNT_REGISTRATION_FAILED(11004, "계좌 등록에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_LIMIT_EXCEEDED(11005, "계좌 등록 한도를 초과했습니다.", HttpStatus.BAD_REQUEST),
    ACCOUNT_VERIFICATION_FAILED(11006, "계좌 인증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT_CREDENTIALS(11007, "잘못된 계좌 인증 정보입니다.", HttpStatus.BAD_REQUEST),
    KIS_CONNECTION_FAILED(11008, "KIS API 연결에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    ACCOUNT_ENCRYPTION_FAILED(11009, "계좌 정보 암호화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_DECRYPTION_FAILED(11010, "계좌 정보 복호화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_DELETION_FAILED(11011, "계좌 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_ALREADY_INACTIVE(11012, "이미 비활성화된 계좌입니다.", HttpStatus.BAD_REQUEST),

    // ==== Portfolio 관련 ====
    PORTFOLIO_NOT_FOUND(12001, "포트폴리오를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PORTFOLIO_ACCESS_DENIED(12002, "포트폴리오 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    PORTFOLIO_ACCOUNT_NOT_FOUND(12003, "포트폴리오에 연결된 계좌를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PORTFOLIO_CREATION_FAILED(12004, "포트폴리오 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PORTFOLIO_UPDATE_FAILED(12005, "포트폴리오 수정에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PORTFOLIO_DELETE_FAILED(12006, "포트폴리오 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PORTFOLIO_LIMIT_EXCEEDED(12007, "포트폴리오 생성 한도를 초과했습니다.", HttpStatus.BAD_REQUEST),
    PORTFOLIO_INVALID_REQUEST(12008, "잘못된 포트폴리오 요청입니다.", HttpStatus.BAD_REQUEST),

    // ==== Trading 관련 ====
    TRADING_ORDER_FAILED(13001, "주식 주문에 실패했습니다.", HttpStatus.BAD_REQUEST),
    KIS_API_ERROR(13002, "KIS API 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    INSUFFICIENT_BALANCE(13003, "잔고가 부족합니다.", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_QUANTITY(13004, "잘못된 주문 수량입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_PRICE(13005, "잘못된 주문 가격입니다.", HttpStatus.BAD_REQUEST),
    TRADING_HOURS_VIOLATION(13006, "거래 시간이 아닙니다.", HttpStatus.BAD_REQUEST),

    // ==== PortfolioStock 관련 ====
    PORTFOLIO_STOCK_NOT_FOUND(14001, "포트폴리오에서 해당 주식을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PORTFOLIO_STOCK_ALREADY_EXISTS(14002, "이미 포트폴리오에 등록된 주식입니다.", HttpStatus.CONFLICT),
    PORTFOLIO_STOCK_ACCESS_DENIED(14003, "포트폴리오 주식 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    TARGET_WEIGHT_SUM_EXCEEDED(14004, "목표 비중의 합이 100%를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST),

        // ==== External API 관련 ====
    EXTERNAL_API_ERROR(15001, "외부 API 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    EXTERNAL_API_TIMEOUT(15002, "외부 API 응답 시간이 초과되었습니다.", HttpStatus.GATEWAY_TIMEOUT),
    EXTERNAL_API_SERVICE_UNAVAILABLE(15003, "외부 API 서비스를 사용할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE),

    // ==== Rebalancing 관련 ====
    REBALANCING_ORDER_NOT_FOUND(15001, "리밸런싱 주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    REBALANCING_ACCESS_DENIED(15002, "리밸런싱 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    TRADE_RECORD_NOT_FOUND(15003, "거래 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ExceptionCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}