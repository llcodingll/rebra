package com.rebra.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.rebra.annotation.LoginUser;
import com.rebra.config.resolver.LoginUserArgumentResolver;
import com.rebra.config.TestSecurityConfig;
import com.rebra.jwt.TokenProvider;
import com.rebra.entity.User;
import com.rebra.jwt.Token;
import com.rebra.service.KakaoOAuth2ServiceImpl;
import com.rebra.service.UserService;
import com.rebra.exception.GlobalExceptionHandler;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private KakaoOAuth2ServiceImpl kakaoOAuth2Service;
    
    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new TestLoginUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
    
    private static class TestLoginUserArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterAnnotation(LoginUser.class) != null &&
                   (Long.class.equals(parameter.getParameterType()) || long.class.equals(parameter.getParameterType()));
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            // requestAttr에서 userId를 추출
            Object userId = webRequest.getAttribute("userId", NativeWebRequest.SCOPE_REQUEST);
            return userId;
        }
    }

    private User createTestUser(Long id, String sub, String nickname) {
        User user = User.builder()
                .sub(sub)
                .nickname(nickname)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
    
    private String createValidJwtToken(Long userId) {
        // 실제 JWT 토큰을 시뮬레이션
        return "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsIm5pY2tOYW1lIjoi7YWM7Iqk7Yq47IKs7Jqp7J6QIn0.mock-signature";
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    @WithMockUser
    void getUserInfo_Success() throws Exception {
        Long userId = 1L;
        User user = createTestUser(userId, "sub123", "테스트사용자");

        given(userService.findById(userId)).willReturn(user);

        mockMvc.perform(get("/api/users/me")
                .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.nickname").value("테스트사용자"));

        verify(userService).findById(userId);
    }

    @Test
    @DisplayName("로그아웃 성공")
    @WithMockUser
    void logout_Success() throws Exception {
        Long userId = 1L;
        User user = createTestUser(userId, "sub123", "테스트사용자");

        given(userService.findById(userId)).willReturn(user);
        doNothing().when(kakaoOAuth2Service).deleteAllRefreshTokensByUser(user);

        mockMvc.perform(post("/api/users/logout")
                .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200));

        verify(userService).findById(userId);
        verify(kakaoOAuth2Service).deleteAllRefreshTokensByUser(user);
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshAccessToken_Success() throws Exception {
        String refreshTokenValue = "valid-refresh-token";
        Token newAccessToken = new Token("new-access-token");
        Token newRefreshToken = new Token("new-refresh-token");
        Token[] tokens = {newAccessToken, newRefreshToken};

        given(kakaoOAuth2Service.refreshTokensWithRotation(refreshTokenValue)).willReturn(tokens);

        mockMvc.perform(post("/api/users/token/refresh")
                .cookie(new Cookie("refreshToken", refreshTokenValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.token").value("new-access-token"))
                .andExpect(cookie().value("accessToken", "new-access-token"))
                .andExpect(cookie().value("refreshToken", "new-refresh-token"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().secure("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("accessToken", "/"))
                .andExpect(cookie().path("refreshToken", "/"));

        verify(kakaoOAuth2Service).refreshTokensWithRotation(refreshTokenValue);
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 리프레시 토큰 없음")
    void refreshAccessToken_MissingRefreshToken_Fails() throws Exception {
        given(kakaoOAuth2Service.refreshTokensWithRotation(null))
                .willThrow(new RuntimeException("Refresh token이 존재하지 않습니다."));
                
        mockMvc.perform(post("/api/users/token/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
    void refreshAccessToken_InvalidToken_Fails() throws Exception {
        String invalidTokenValue = "invalid-refresh-token";

        given(kakaoOAuth2Service.refreshTokensWithRotation(invalidTokenValue))
                .willThrow(new RuntimeException("Refresh token이 존재하지 않습니다."));

        mockMvc.perform(post("/api/users/token/refresh")
                .cookie(new Cookie("refreshToken", invalidTokenValue)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 실패 - 사용자 없음")
    @WithMockUser
    void logout_UserNotFound_Fails() throws Exception {
        Long userId = 999L;

        given(userService.findById(userId)).willThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        mockMvc.perform(post("/api/users/logout")
                .requestAttr("userId", userId))
                .andExpect(status().isInternalServerError());

        verify(userService).findById(userId);
    }
}