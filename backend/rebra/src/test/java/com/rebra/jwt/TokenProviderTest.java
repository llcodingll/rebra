package com.rebra.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rebra.dto.TempToken;
import com.rebra.entity.User;
import com.rebra.exception.BusinessException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TokenProviderTest {

    private static final String JWT_SECRET = "dGVzdC1qd3Qtc2VjcmV0LWtleS10ZXN0LWp3dC1zZWNyZXQta2V5LXRlc3QtaW5ncmVk";
    private static final String SUB = "sub123";
    private static final String NICKNAME = "테스트사용자";
    private static final String MALFORMED_TOKEN = "thisIsNotAJwt";
    private static final String FAKE_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF9.wrong_signature";

    private static final int ACCESS_TOKEN_MINUTES = 30;
    private static final int REFRESH_TOKEN_MINUTES = 10080;

    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecretKey", JWT_SECRET);
        ReflectionTestUtils.setField(tokenProvider, "accessTokenExpireMinutes", ACCESS_TOKEN_MINUTES);
        ReflectionTestUtils.setField(tokenProvider, "refreshTokenExpireMinutes", REFRESH_TOKEN_MINUTES);
    }

    private User createUser(Long id, String sub, String nickname) {
        User user = User.builder()
                .sub(sub)
                .nickname(nickname)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("액세스 토큰 생성 성공")
    void generateAccessToken_Success() {
        User user = createUser(1L, SUB, NICKNAME);

        Token token = tokenProvider.generateAccessToken(user);

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertTrue(tokenProvider.validateToken(token.getToken()));
    }

    @Test
    @DisplayName("리프레시 토큰 생성 성공")
    void generateRefreshToken_Success() {
        User user = createUser(1L, SUB, NICKNAME);

        Token token = tokenProvider.generateRefreshToken(user);

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertTrue(tokenProvider.validateToken(token.getToken()));
    }

    @Test
    @DisplayName("임시 토큰 생성 및 파싱 성공")
    void generateTempToken_Success() {
        TempToken tempToken = new TempToken(SUB);

        Token token = tokenProvider.generateTempToken(tempToken);
        
        assertNotNull(token);
        assertNotNull(token.getToken());
        
        TempToken parsed = tokenProvider.getTempTokenData(token.getToken());
        assertEquals(SUB, parsed.getSub());
        assertFalse(parsed.isExpired());
    }

    @Test
    @DisplayName("만료된 임시 토큰 파싱 시 예외 발생")
    void getTempTokenData_ExpiredToken_ThrowsException() {
        TempToken expiredTempToken = new TempToken(SUB, LocalDateTime.now().minusMinutes(31));

        Token token = tokenProvider.generateTempToken(expiredTempToken);

        assertThrows(BusinessException.class, 
            () -> tokenProvider.getTempTokenData(token.getToken()));
    }

    @Test
    @DisplayName("일반 토큰을 임시 토큰으로 파싱 시 예외 발생")
    void getTempTokenData_InvalidTokenType_ThrowsException() {
        User user = createUser(1L, SUB, NICKNAME);
        Token accessToken = tokenProvider.generateAccessToken(user);

        assertThrows(BusinessException.class, 
            () -> tokenProvider.getTempTokenData(accessToken.getToken()));
    }

    @Test
    @DisplayName("JWT 생성 및 사용자 ID 추출 성공")
    void generateToken_AndExtractUserId_Success() {
        User user = createUser(42L, SUB, NICKNAME);

        Token token = tokenProvider.generateToken(user, 60);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token.getToken()));

        Long userId = tokenProvider.getUserIdFromToken(token.getToken());
        assertEquals(42L, userId);
    }

    @Test
    @DisplayName("유효한 JWT 검증 성공")
    void validateToken_ValidToken_ReturnsTrue() {
        User user = createUser(1L, SUB, NICKNAME);
        Token token = tokenProvider.generateAccessToken(user);

        boolean isValid = tokenProvider.validateToken(token.getToken());

        assertTrue(isValid);
    }

    @Test
    @DisplayName("만료된 JWT 검증 시 ExpiredJwtException 발생")
    void validateToken_ExpiredToken_ThrowsExpiredJwtException() throws InterruptedException {
        User user = createUser(1L, SUB, NICKNAME);

        Token token = tokenProvider.generateToken(user, 0); // 0분 → 즉시 만료
        Thread.sleep(1000); // 1초 대기

        assertThrows(ExpiredJwtException.class, 
            () -> tokenProvider.validateToken(token.getToken()));
    }

    @Test
    @DisplayName("서명이 틀린 JWT 검증 시 SignatureException 발생")
    void validateToken_InvalidSignature_ThrowsSignatureException() {
        assertThrows(SignatureException.class, 
            () -> tokenProvider.validateToken(FAKE_TOKEN));
    }

    @Test
    @DisplayName("잘못된 형식의 JWT 검증 시 MalformedJwtException 발생")
    void validateToken_MalformedToken_ThrowsMalformedJwtException() {
        assertThrows(MalformedJwtException.class, 
            () -> tokenProvider.validateToken(MALFORMED_TOKEN));
    }

    @Test
    @DisplayName("null 토큰 검증 시 예외 발생")
    void validateToken_NullToken_ThrowsException() {
        assertThrows(Exception.class, 
            () -> tokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("빈 토큰 검증 시 예외 발생")
    void validateToken_EmptyToken_ThrowsException() {
        assertThrows(Exception.class, 
            () -> tokenProvider.validateToken(""));
    }

    @Test
    @DisplayName("유효한 JWT에서 사용자 ID 추출 성공")
    void getUserIdFromToken_ValidToken_ReturnsUserId() {
        User user = createUser(123L, SUB, NICKNAME);
        Token token = tokenProvider.generateAccessToken(user);

        Long userId = tokenProvider.getUserIdFromToken(token.getToken());

        assertEquals(123L, userId);
    }

    @Test
    @DisplayName("만료된 JWT에서 사용자 ID 추출 시 예외 발생")
    void getUserIdFromToken_ExpiredToken_ThrowsException() throws InterruptedException {
        User user = createUser(1L, SUB, NICKNAME);
        Token token = tokenProvider.generateToken(user, 0);
        Thread.sleep(1000);

        assertThrows(Exception.class, 
            () -> tokenProvider.getUserIdFromToken(token.getToken()));
    }

    @Test
    @DisplayName("잘못된 JWT에서 사용자 ID 추출 시 예외 발생")
    void getUserIdFromToken_InvalidToken_ThrowsException() {
        assertThrows(Exception.class, 
            () -> tokenProvider.getUserIdFromToken(FAKE_TOKEN));
    }

    @Test
    @DisplayName("토큰 Claims 구조 확인")
    void tokenClaimsStructure_Test() {
        User user = createUser(999L, SUB, NICKNAME);
        Token token = tokenProvider.generateAccessToken(user);

        // 토큰이 정상적으로 생성되고 검증되는지 확인
        assertTrue(tokenProvider.validateToken(token.getToken()));
        
        // 사용자 ID가 올바르게 추출되는지 확인  
        Long extractedUserId = tokenProvider.getUserIdFromToken(token.getToken());
        assertEquals(999L, extractedUserId);
    }

    @Test
    @DisplayName("다른 사용자의 토큰으로 잘못된 사용자 ID 추출 방지")
    void getUserIdFromToken_DifferentUsers_ReturnCorrectIds() {
        User user1 = createUser(100L, "sub1", "user1");
        User user2 = createUser(200L, "sub2", "user2");

        Token token1 = tokenProvider.generateAccessToken(user1);
        Token token2 = tokenProvider.generateAccessToken(user2);

        Long userId1 = tokenProvider.getUserIdFromToken(token1.getToken());
        Long userId2 = tokenProvider.getUserIdFromToken(token2.getToken());

        assertEquals(100L, userId1);
        assertEquals(200L, userId2);
    }

    @Test
    @DisplayName("임시 토큰 만료 시간 검증")
    void tempToken_ExpirationTime_Test() {
        // 만료되지 않은 임시 토큰
        TempToken validTempToken = new TempToken(SUB);
        Token validToken = tokenProvider.generateTempToken(validTempToken);
        
        TempToken parsed = tokenProvider.getTempTokenData(validToken.getToken());
        assertFalse(parsed.isExpired());

        // 만료된 임시 토큰
        TempToken expiredTempToken = new TempToken(SUB, LocalDateTime.now().minusHours(1));
        Token expiredToken = tokenProvider.generateTempToken(expiredTempToken);
        
        assertThrows(BusinessException.class, 
            () -> tokenProvider.getTempTokenData(expiredToken.getToken()));
    }

    @Test
    @DisplayName("리프레시 토큰 만료 시간 설정값 확인")
    void getRefreshTokenExpireMinutes_ReturnsCorrectValue() {
        int expireMinutes = tokenProvider.getRefreshTokenExpireMinutes();
        assertEquals(REFRESH_TOKEN_MINUTES, expireMinutes);
    }
}