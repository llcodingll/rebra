package com.rebra.service;

import com.rebra.dto.response.TokenRefreshResponse;
import com.rebra.jwt.Token;

public interface TokenService {
    
    Token generateAccessToken(Long userId);
    
    Token generateRefreshToken(Long userId);
    
    TokenRefreshResponse refreshTokensWithRotation(String oldRefreshToken);
    
    void deleteAllUserRefreshTokens(Long userId);
    
    Long validateRefreshTokenAndGetUserId(String refreshToken);
    
    void saveRefreshTokenForUser(Long userId, Token refreshToken);
    
    TokenRefreshResponse issueNewTokensForUser(Long userId);
}