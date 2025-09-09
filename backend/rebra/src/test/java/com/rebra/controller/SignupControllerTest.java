package com.rebra.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.dto.TempToken;
import com.rebra.dto.request.SignupRequest;
import com.rebra.entity.SurveyResult;
import com.rebra.entity.User;
import com.rebra.jwt.Token;
import com.rebra.jwt.TokenProvider;
import com.rebra.service.KakaoOAuth2ServiceImpl;
import com.rebra.service.SignupService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SignupController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SignupService signupService;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private KakaoOAuth2ServiceImpl kakaoOAuth2Service;

    private User createTestUser(Long id, String sub, String nickname) {
        SurveyResult surveyResult = SurveyResult.builder()
                .age(30)
                .mainIncomeSource("급여소득")
                .investmentPurpose(25)
                .investmentExperience(26)
                .riskTolerance(25)
                .build();
                
        User user = User.builder()
                .sub(sub)
                .nickname(nickname)
                .surveyResult(surveyResult)
                .build();
        // Entity의 ID는 ReflectionTestUtils 필요 (JPA auto-generated field, setter 없음)
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 사용 가능")
    void checkNicknameAvailability_Available() throws Exception {
        String nickname = "사용가능닉네임";
        
        given(signupService.isNicknameAvailable(nickname)).willReturn(true);

        mockMvc.perform(get("/auth/nickname/check")
                .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 사용 불가능")
    void checkNicknameAvailability_NotAvailable() throws Exception {
        String nickname = "중복된닉네임";
        
        given(signupService.isNicknameAvailable(nickname)).willReturn(false);

        mockMvc.perform(get("/auth/nickname/check")
                .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    @DisplayName("회원가입 완료 성공")
    void completeSignup_Success() throws Exception {
        String tempTokenValue = "valid-temp-token";
        String nickname = "신규사용자";
        String kakaoSub = "kakao-sub-123";
        
        SignupRequest request = new SignupRequest(nickname, 30, "급여소득", 25, 26, 25);
        
        TempToken tempTokenData = new TempToken(kakaoSub);
        User createdUser = createTestUser(1L, kakaoSub, nickname);
        Token refreshToken = new Token("refresh-token");
        Token accessToken = new Token("access-token");

        given(tokenProvider.getTempTokenData(tempTokenValue)).willReturn(tempTokenData);
        given(signupService.isNicknameAvailable(nickname)).willReturn(true);
        given(kakaoOAuth2Service.createUserWithKakaoSub(kakaoSub, request)).willReturn(createdUser);
        given(tokenProvider.generateRefreshToken(createdUser)).willReturn(refreshToken);
        given(kakaoOAuth2Service.issueAccessToken("refresh-token")).willReturn(accessToken);
        // saveRefreshTokenForUser는 void 메서드이므로 아무것도 하지 않음

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("tempToken", tempTokenValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.message").value("회원가입이 완료되었습니다."))
                .andExpect(cookie().value("refreshToken", "refresh-token"))
                .andExpect(cookie().value("accessToken", "access-token"))
                .andExpect(cookie().maxAge("tempToken", 0))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().secure("accessToken", true))
                .andExpect(cookie().path("refreshToken", "/"))
                .andExpect(cookie().path("accessToken", "/"));
    }

    @Test
    @DisplayName("회원가입 실패 - 임시 토큰 없음")
    void completeSignup_MissingTempToken_Fails() throws Exception {
        String nickname = "신규사용자";
        SignupRequest request = new SignupRequest(nickname, 30, "급여소득", 25, 26, 25);

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 닉네임")
    void completeSignup_DuplicateNickname_Fails() throws Exception {
        String tempTokenValue = "valid-temp-token";
        String nickname = "중복닉네임";
        String kakaoSub = "kakao-sub-123";
        
        SignupRequest request = new SignupRequest(nickname, 30, "급여소득", 25, 26, 25);
        
        TempToken tempTokenData = new TempToken(kakaoSub);

        given(tokenProvider.getTempTokenData(tempTokenValue)).willReturn(tempTokenData);
        given(signupService.isNicknameAvailable(nickname)).willReturn(false);

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("tempToken", tempTokenValue)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 유효하지 않은 임시 토큰")
    void completeSignup_InvalidTempToken_Fails() throws Exception {
        String tempTokenValue = "invalid-temp-token";
        String nickname = "신규사용자";
        
        SignupRequest request = new SignupRequest(nickname, 30, "급여소득", 25, 26, 25);

        given(tokenProvider.getTempTokenData(tempTokenValue))
                .willThrow(new RuntimeException("Invalid temp token"));

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("tempToken", tempTokenValue)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("회원가입 실패 - 빈 닉네임")
    void completeSignup_EmptyNickname_Fails() throws Exception {
        String tempTokenValue = "valid-temp-token";
        
        SignupRequest request = new SignupRequest("", 30, "급여소득", 25, 26, 25);

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("tempToken", tempTokenValue)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - null 닉네임")
    void completeSignup_NullNickname_Fails() throws Exception {
        String tempTokenValue = "valid-temp-token";
        
        SignupRequest request = new SignupRequest(null, 30, "급여소득", 25, 26, 25);

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(new Cookie("tempToken", tempTokenValue)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("닉네임 파라미터 누락")
    void checkNicknameAvailability_MissingParameter_Fails() throws Exception {
        mockMvc.perform(get("/auth/nickname/check"))
                .andExpect(status().isBadRequest());
    }
}