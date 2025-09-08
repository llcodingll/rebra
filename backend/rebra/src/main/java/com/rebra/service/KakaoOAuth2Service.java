package com.rebra.service;


import com.rebra.dto.response.KakaoTokenResponse;
import com.rebra.dto.response.LoginResponse;
import com.rebra.jwt.Token;
import jakarta.servlet.http.HttpSession;

public interface KakaoOAuth2Service {

    String buildKakaoAuthorizeUrlAndSaveNonceInSession(HttpSession session);

    LoginResponse processLogin(KakaoTokenResponse kakaoTokenResponse, HttpSession session);

    Token issueAccessTokenByValidRefreshToken(String refreshTokenValue);

//    void softDeleteUserAndRemoveAllRefreshTokens(Long userId);
    Token[] refreshTokensWithRotation(String oldRefreshToken);


    Token issueAccessToken(String token);

    Token generateTempTokenForSignup(String kakaoSub);

    KakaoTokenResponse fetchKakaoTokenByAuthorizationCode(String code);
}