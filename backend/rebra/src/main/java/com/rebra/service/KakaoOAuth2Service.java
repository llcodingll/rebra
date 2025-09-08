package com.rebra.service;


import com.rebra.dto.response.LoginResponse;
import com.rebra.dto.response.UserProfileResponse;
import com.rebra.jwt.Token;
import jakarta.servlet.http.HttpSession;

public interface KakaoOAuth2Service {

    String buildKakaoAuthorizeUrlAndSaveNonceInSession(HttpSession session);

    LoginResponse exchangeAuthorizationCodeForLoginAndCreateUserIfNeeded(String authorizationCode, HttpSession session);

    Token issueAccessTokenByValidRefreshToken(String refreshTokenValue);

//    void softDeleteUserAndRemoveAllRefreshTokens(Long userId);

    UserProfileResponse getUserProfile(Long userId);

    Token issueAccessToken(String token);
}