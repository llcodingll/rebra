package com.rebra.jwt;

import com.rebra.dto.TempToken;
import com.rebra.entity.User;
import com.rebra.exception.BusinessException;
import static com.rebra.exception.ExceptionCode.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.time.Duration;
import java.util.Date;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenProvider {

    @Value("${jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${jwt.ACCESS_TOKEN_MINUTE_TIME}")
    private int accessTokenExpireMinutes;

    @Getter
    @Value("${jwt.REFRESH_TOKEN_MINUTE_TIME}")
    private int refreshTokenExpireMinutes;
    
    private static final int TEMP_TOKEN_EXPIRE_MINUTES = 30;
    
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public Token generateAccessToken(User user) {
        return generateToken(user, accessTokenExpireMinutes);
    }

    public Token generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpireMinutes);
    }

    public Token generateToken(User user, int minutes) {
        Duration expiredAt = Duration.ofMinutes(minutes);
        Date now = new Date();
        String token = makeToken(user, new Date(now.getTime() + expiredAt.toMillis()));
        log.debug("Generated JWT for userId={}, expiresAt={}", user.getId(),
                new Date(now.getTime() + expiredAt.toMillis()));
        return new Token(token);
    }

    private String makeToken(User user, Date expiry) {
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("userId", user.getId())
                .claim("nickName", user.getNickname())
                .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        log.debug("JWT 토큰 유효성 검사 시작");
        Claims claims = Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
        log.debug("JWT 유효함");
        return true;
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
            log.info("JWT claims 파싱 결과: {}", claims);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            log.error("JWT에서 userId 추출 실패: {}", e.getMessage(), e);
            throw new BusinessException(INVALID_TEMP_TOKEN);
        }
    }
    
    // 임시 토큰 생성 (카카오 회원가입용)
    public Token generateTempToken(TempToken tempTokenData) {
        try {
            String tempTokenJson = objectMapper.writeValueAsString(tempTokenData);
            Duration expiredAt = Duration.ofMinutes(TEMP_TOKEN_EXPIRE_MINUTES);
            Date now = new Date();
            
            String token = Jwts.builder()
                    .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                    .setIssuedAt(now)
                    .setExpiration(new Date(now.getTime() + expiredAt.toMillis()))
                    .claim("tempToken", tempTokenJson)
                    .claim("type", "TEMP")
                    .signWith(SignatureAlgorithm.HS256, jwtSecretKey)
                    .compact();
            
            log.debug("Generated temp token for sub={}, expiresAt={}", 
                    tempTokenData.getSub(), tempTokenData.getExpireAt());
            return new Token(token);
        } catch (JsonProcessingException e) {
            log.error("임시 토큰 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("임시 토큰 생성 실패", e);
        }
    }
    
    // 임시 토큰에서 TempToken 데이터 추출
    public TempToken getTempTokenData(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
            String type = claims.get("type", String.class);
            
            if (!"TEMP".equals(type)) {
                throw new BusinessException(INVALID_TOKEN_TYPE);
            }
            
            String tempTokenJson = claims.get("tempToken", String.class);
            TempToken tempToken = objectMapper.readValue(tempTokenJson, TempToken.class);
            
            if (tempToken.isExpired()) {
                throw new BusinessException(EXPIRED_TEMP_TOKEN);
            }
            
            return tempToken;
        } catch (JsonProcessingException e) {
            log.error("임시 토큰 파싱 실패: {}", e.getMessage(), e);
            throw new BusinessException(TEMP_TOKEN_PARSING_FAILED);
        } catch (ExpiredJwtException e) {
            log.info("만료된 임시 토큰: {}", e.getMessage());
            throw new BusinessException(EXPIRED_TEMP_TOKEN);
        } catch (BusinessException e) {
            // BusinessException은 그대로 재전파
            throw e;
        } catch (Exception e) {
            log.error("임시 토큰 검증 실패: {}", e.getMessage(), e);
            throw new BusinessException(INVALID_TEMP_TOKEN);
        }
    }

}