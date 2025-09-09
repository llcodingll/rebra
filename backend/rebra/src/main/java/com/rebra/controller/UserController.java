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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User API", description = "사용자 관리 API")
public class UserController {


    private final UserService userService;
    private final KakaoOAuth2ServiceImpl kakaoOAuth2Service;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/me")
    public ResponseEntity<CommonApiResponse<UserProfileResponse>> getUserInfo(@LoginUser Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(CommonApiResponse.success(
            new UserProfileResponse(user.getId(), user.getNickname())
        ));
    }

    @Operation(summary = "로그아웃", description = "사용자를 로그아웃하고 모든 리프레시 토큰을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "로그아웃 처리 중 오류 발생")
    })
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

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다. (RTR 적용)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "401", description = "리프레시 토큰이 유효하지 않거나 만료됨")
    })
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
