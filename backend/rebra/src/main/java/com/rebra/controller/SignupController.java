package com.rebra.controller;

import com.rebra.common.CommonApiResponse;
import com.rebra.dto.TempToken;
import com.rebra.dto.request.SignupRequest;
import com.rebra.dto.response.SignupResponse;
import com.rebra.entity.User;
import com.rebra.jwt.Token;
import com.rebra.jwt.TokenProvider;
import com.rebra.service.KakaoOAuth2ServiceImpl;
import com.rebra.service.SignupService;
import com.rebra.util.CookieUtil;
import static com.rebra.util.CookieUtil.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class SignupController {
    private final SignupService signupService;
    private final TokenProvider tokenProvider;
    private final KakaoOAuth2ServiceImpl kakaoOAuth2Service;

    @GetMapping("/nickname/check")
    public ResponseEntity<Boolean> checkNicknameAvailability(@RequestParam String nickname) {
        boolean isAvailable = signupService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(isAvailable);
    }

    @PostMapping("/signup")
    public ResponseEntity<CommonApiResponse<SignupResponse>> completeSignup(
            @Valid @RequestBody SignupRequest signupRequest,
            @CookieValue(TEMP_TOKEN_COOKIE_NAME) String tempToken,
            HttpServletResponse response) {
        
        // 임시 토큰에서 kakaoSub 추출
        TempToken tempTokenData = tokenProvider.getTempTokenData(tempToken);
        
        // 닉네임 중복 확인
        if (!signupService.isNicknameAvailable(signupRequest.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        
        // 실제 사용자 생성
        User user = kakaoOAuth2Service.createUserWithKakaoSub(tempTokenData.getSub(), signupRequest.getNickname());
        
        // Access Token + Refresh Token
        Token refreshToken = tokenProvider.generateRefreshToken(user);
        kakaoOAuth2Service.saveRefreshTokenForUser(user, refreshToken);
        Token accessToken = kakaoOAuth2Service.issueAccessToken(refreshToken.getToken());
        
        // 쿠키 설정
        CookieUtil.addRefreshTokenCookie(response, refreshToken.getToken());
        CookieUtil.addAccessTokenCookie(response, accessToken.getToken());
        CookieUtil.deleteTempTokenCookie(response); // 임시 토큰 삭제
        
        log.info("회원가입 완료: userId={}, nickname={}", user.getId(), user.getNickname());
        
        SignupResponse signupResponse = new SignupResponse(user.getId(), user.getNickname(), "회원가입이 완료되었습니다.");
        return ResponseEntity.ok(CommonApiResponse.success(signupResponse));
    }

}