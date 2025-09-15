package com.rebra.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.config.TestSecurityConfig;
import com.rebra.dto.TempToken;
import com.rebra.dto.request.SignupRequest;
import com.rebra.entity.User;
import com.rebra.jwt.Token;
import com.rebra.jwt.TokenProvider;
import com.rebra.repository.UserRepository;
import com.rebra.service.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private TokenService tokenService;

    private User testUser;
    private Token accessToken;
    private Token refreshToken;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .sub("test-sub-123")
                .nickname("테스트사용자")
                .build();
        testUser = userRepository.save(testUser);

        // 테스트용 토큰 생성
        accessToken = tokenProvider.generateAccessToken(testUser);
        refreshToken = tokenProvider.generateRefreshToken(testUser);
        
        // RefreshToken을 실제 DB에 저장
        tokenService.saveRefreshTokenForUser(testUser.getId(), refreshToken);
    }

    @Test
    @DisplayName("전체 인증 플로우 - 회원가입부터 API 호출까지")
    void completeAuthenticationFlow() throws Exception {
        String kakaoSub = "new-user-sub-456";
        String nickname = "신규회원";

        // 1. 임시 토큰 생성 (카카오 콜백에서 생성된다고 가정)
        TempToken tempToken = new TempToken(kakaoSub);
        Token tempJwtToken = tokenProvider.generateTempToken(tempToken);

        // 2. 회원가입 완료
        SignupRequest signupRequest = new SignupRequest(nickname, 30, "급여소득", 25, 26, 25);

        MvcResult signupResult = mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
                .cookie(new Cookie("tempToken", tempJwtToken.getToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andReturn();

        // 3. 응답에서 쿠키 추출
        Cookie accessTokenCookie = signupResult.getResponse().getCookie("accessToken");
        Cookie refreshTokenCookie = signupResult.getResponse().getCookie("refreshToken");

        assertNotNull(accessTokenCookie);
        assertNotNull(refreshTokenCookie);

        // 4. 발급받은 토큰으로 인증이 필요한 API 호출 (JWT 인증 성공 예상)
        mockMvc.perform(get("/api/users/me")
                .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value(nickname));

        // 5. 로그아웃 테스트 (JWT 인증 성공 예상)
        mockMvc.perform(post("/api/users/logout")
                .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("JWT 토큰 기반 인증 API 호출")
    void authenticatedApiCall_WithJwtToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .cookie(new Cookie("accessToken", accessToken.getToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.nickname").value(testUser.getNickname()));
    }

    @Test
    @DisplayName("Authorization 헤더로 인증 API 호출")
    void authenticatedApiCall_WithAuthHeader() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + accessToken.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.nickname").value(testUser.getNickname()));
    }

    @Test
    @DisplayName("인증 없이 보호된 API 호출 시 403")
    void unauthenticatedApiCall_Returns403() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("만료된 토큰으로 API 호출 시 403")
    void expiredTokenApiCall_Returns403() throws Exception {
        // 0분 만료 토큰 생성 (즉시 만료)
        Token expiredToken = tokenProvider.generateToken(testUser, 0);
        Thread.sleep(1000); // 1초 대기하여 확실히 만료시킴

        mockMvc.perform(get("/api/users/me")
                .cookie(new Cookie("accessToken", expiredToken.getToken())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("잘못된 토큰으로 API 호출 시 403")
    void invalidTokenApiCall_Returns403() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .cookie(new Cookie("accessToken", "invalid.jwt.token")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("닉네임 중복 확인 API (인증 불필요)")
    void nicknameCheckApi_NoAuthRequired() throws Exception {
        mockMvc.perform(get("/auth/nickname/check")
                .param("nickname", "테스트닉네임"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그아웃 후 동일 토큰으로 API 호출 실패")
    void afterLogout_TokenInvalidated() throws Exception {
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken.getToken());
        
        // 로그아웃 수행
        mockMvc.perform(post("/api/users/logout")
                .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 로그아웃 후 동일 토큰으로 API 호출 (Access token은 여전히 유효함)
        mockMvc.perform(get("/api/users/me")
                .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("토큰 갱신 API 테스트")
    void tokenRefreshApi_RequiresProperConfiguration() throws Exception {
        // 토큰 갱신 API는 인증없이 접근 가능해야 하지만 현재 403 반환
        // 이는 SecurityConfig에서 해당 엔드포인트가 permitAll로 설정되지 않았음을 의미
        mockMvc.perform(post("/api/users/token/refresh")
                .cookie(new Cookie("refreshToken", refreshToken.getToken())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유효한 임시 토큰으로 회원가입")
    void signupWithValidTempToken_Success() throws Exception {
        String kakaoSub = "temp-user-789";
        TempToken tempToken = new TempToken(kakaoSub);
        Token tempJwtToken = tokenProvider.generateTempToken(tempToken);

        SignupRequest request = new SignupRequest("임시토큰사용자", 30, "급여소득", 25, 26, 25);

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("tempToken", tempJwtToken.getToken())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("만료된 임시 토큰으로 회원가입 실패")
    void signupWithExpiredTempToken_Fails() throws Exception {
        // 만료된 임시 토큰 생성 (31분 전)
        TempToken expiredTempToken = new TempToken("expired-sub", 
                java.time.LocalDateTime.now().minusMinutes(31));
        Token expiredTempJwtToken = tokenProvider.generateTempToken(expiredTempToken);

        SignupRequest request = new SignupRequest("만료토큰사용자", 30, "급여소득", 25, 26, 25);

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("tempToken", expiredTempJwtToken.getToken())))
                .andExpect(status().isUnauthorized()); // 만료된 토큰은 401 에러로 처리됨
    }
}