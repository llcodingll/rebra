package com.rebra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rebra.dto.TempToken;
import com.rebra.dto.response.KakaoTokenResponse;
import com.rebra.entity.User;
import com.rebra.jwt.Token;
import com.rebra.jwt.TokenProvider;
import com.rebra.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class KakaoOAuth2ServiceImplTest {

    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    
    private static final String USER_SUB_EXISTING = "existing-user-sub";
    private static final String USER_SUB_NEW = "new-user-sub";
    private static final String NICKNAME_EXISTING_USER = "기존사용자";
    private static final String NICKNAME_NEW_USER = "신규사용자";
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TokenProvider tokenProvider;
    
    @Mock
    private HttpSession session;
    
    @InjectMocks
    private KakaoOAuth2ServiceImpl kakaoOAuth2Service;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kakaoOAuth2Service, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(kakaoOAuth2Service, "clientSecret", CLIENT_SECRET);
        ReflectionTestUtils.setField(kakaoOAuth2Service, "redirectUri", REDIRECT_URI);
        ReflectionTestUtils.setField(kakaoOAuth2Service, "tokenUrl", TOKEN_URL);
    }

    private User createUser(Long id, String sub, String nickname) {
        User user = User.builder()
                .sub(sub)
                .nickname(nickname)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("카카오 인증 URL 생성 성공")
    void buildKakaoAuthorizeUrlAndSaveNonceInSession_Success() {
        String result = kakaoOAuth2Service.buildKakaoAuthorizeUrlAndSaveNonceInSession(session);

        assertNotNull(result);
        assertThat(result).contains("https://kauth.kakao.com/oauth/authorize");
        assertThat(result).contains("client_id=" + CLIENT_ID);
        assertThat(result).contains("redirect_uri=" + REDIRECT_URI);
        assertThat(result).contains("response_type=code");
        assertThat(result).contains("nonce=");
    }

    @Test
    @DisplayName("기존 사용자 로그인 처리 성공")
    void processUserLogin_ExistingUser_ReturnsUserId() {
        // given
        User existingUser = createUser(1L, USER_SUB_EXISTING, NICKNAME_EXISTING_USER);
        given(userRepository.findBySub(USER_SUB_EXISTING)).willReturn(Optional.of(existingUser));

        // when
        Long result = kakaoOAuth2Service.processUserLogin(USER_SUB_EXISTING);

        // then
        assertThat(result).isEqualTo(1L);
        verify(userRepository).findBySub(USER_SUB_EXISTING);
    }

    @Test
    @DisplayName("신규 사용자 로그인 처리 - null 반환")
    void processUserLogin_NewUser_ReturnsNull() {
        // given
        given(userRepository.findBySub(USER_SUB_NEW)).willReturn(Optional.empty());

        // when
        Long result = kakaoOAuth2Service.processUserLogin(USER_SUB_NEW);

        // then
        assertThat(result).isNull();
        verify(userRepository).findBySub(USER_SUB_NEW);
    }


    @Test
    @DisplayName("임시 토큰 생성 성공")
    void generateTempTokenForSignup_Success() {
        // given
        Token tempToken = new Token("temp-token-value");
        given(tokenProvider.generateTempToken(any(TempToken.class))).willReturn(tempToken);

        // when
        Token result = kakaoOAuth2Service.generateTempTokenForSignup(USER_SUB_NEW);

        // then
        assertNotNull(result);
        assertThat(result.getToken()).isEqualTo("temp-token-value");
        verify(tokenProvider).generateTempToken(any(TempToken.class));
    }



    @Test
    @DisplayName("카카오 토큰 요청 성공")
    void fetchKakaoTokenByAuthorizationCode_Success() {
        // given
        String authCode = "authorization-code";
        KakaoTokenResponse expectedResponse = new KakaoTokenResponse(
                "Bearer", "access-token", "id-token", 3600, "refresh-token", 604800, "openid");

        RequestBodyUriSpec requestBodyUriSpec = org.mockito.Mockito.mock(RequestBodyUriSpec.class);
        RequestBodySpec requestBodySpec = org.mockito.Mockito.mock(RequestBodySpec.class);
        ResponseSpec responseSpec = org.mockito.Mockito.mock(ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(TOKEN_URL)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(org.springframework.web.reactive.function.BodyInserter.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(KakaoTokenResponse.class)).thenReturn(Mono.just(expectedResponse));

        // when
        KakaoTokenResponse result = kakaoOAuth2Service.fetchKakaoTokenByAuthorizationCode(authCode);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse.getAccessToken(), result.getAccessToken());
        assertEquals(expectedResponse.getIdToken(), result.getIdToken());
    }

}