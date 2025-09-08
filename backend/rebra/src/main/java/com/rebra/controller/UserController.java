package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.response.UserProfileResponse;
import com.rebra.entity.User;
import com.rebra.jwt.Token;
import com.rebra.service.KakaoOAuth2ServiceImpl;
import com.rebra.service.UserService;
import com.rebra.util.CookieUtil;
import static com.rebra.util.CookieUtil.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {


    private final UserService userService;
    private final KakaoOAuth2ServiceImpl kakaoOAuth2Service;

    @GetMapping("/me")
    public ResponseEntity<CommonApiResponse<UserProfileResponse>> getUserInfo(@LoginUser Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(CommonApiResponse.success(new UserProfileResponse(user.getId(), user.getNickname())));
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonApiResponse<Void>> logout(@LoginUser Long userId, HttpServletResponse response) {
        try {
            // 모든 리프레시 토큰 삭제
            User user = userService.findById(userId);
            kakaoOAuth2Service.deleteAllRefreshTokensByUser(user);
            
            // 쿠키 삭제
            CookieUtil.deleteAccessTokenCookie(response);
            CookieUtil.deleteRefreshTokenCookie(response);
            
            return ResponseEntity.ok(CommonApiResponse.success());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(CommonApiResponse.error("LOGOUT_FAILED", "로그아웃 실패", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<CommonApiResponse<Token>> refreshAccessToken(
            @CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenValue,
            HttpServletResponse response) {

        try {
            // RTR 적용 토큰 갱신
            Token[] tokens = kakaoOAuth2Service.refreshTokensWithRotation(refreshTokenValue);
            Token newAccessToken = tokens[0];
            Token newRefreshToken = tokens[1];
            
            // 새로운 토큰들을 쿠키에 설정
            CookieUtil.addRefreshTokenCookie(response, newRefreshToken.getToken());
            CookieUtil.addAccessTokenCookie(response, newAccessToken.getToken());
            
            return ResponseEntity.ok(CommonApiResponse.success(newAccessToken));
        } catch (Exception e) {
            log.warn("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonApiResponse.error("TOKEN_REFRESH_FAILED", "토큰 갱신 실패", HttpStatus.UNAUTHORIZED));
        }
    }
}
