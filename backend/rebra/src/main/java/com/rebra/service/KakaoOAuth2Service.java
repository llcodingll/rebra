package com.rebra.service;

import com.rebra.dto.response.KakaoTokenResponse;
import com.rebra.jwt.Token;
import jakarta.servlet.http.HttpSession;

public interface KakaoOAuth2Service {

    String buildKakaoAuthorizeUrlAndSaveNonceInSession(HttpSession session);

    Long processUserLogin(String kakaoSub);

    Token generateTempTokenForSignup(String kakaoSub);

    KakaoTokenResponse fetchKakaoTokenByAuthorizationCode(String code);
}