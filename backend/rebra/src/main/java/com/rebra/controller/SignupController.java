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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Signup API", description = "회원가입 관련 API")
public class SignupController {
    private final SignupService signupService;
    private final TokenProvider tokenProvider;
    private final KakaoOAuth2ServiceImpl kakaoOAuth2Service;

    @Operation(summary = "닉네임 중복 확인", description = "입력한 닉네임의 중복 여부를 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "중복 확인 완료"),
        @ApiResponse(responseCode = "400", description = "잘못된 닉네임 형식")
    })
    @GetMapping("/nickname/check")
    public ResponseEntity<Boolean> checkNicknameAvailability(
            @Parameter(description = "중복 확인할 닉네임", required = true, example = "테스트사용자")
            @RequestParam String nickname) {
        boolean isAvailable = signupService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(isAvailable);
    }

    @Operation(summary = "회원가입 완료", description = "카카오 로그인 후 닉네임을 설정하여 회원가입을 완료합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "중복된 닉네임 또는 잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "임시 토큰이 유효하지 않거나 만료됨")
    })
    @PostMapping("/signup")
    public ResponseEntity<CommonApiResponse<SignupResponse>> completeSignup(
            @Parameter(description = "회원가입 요청 정보", required = true)
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