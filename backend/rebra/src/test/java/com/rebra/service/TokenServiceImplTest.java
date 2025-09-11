package com.rebra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rebra.dto.response.TokenRefreshResponse;
import com.rebra.entity.RefreshToken;
import com.rebra.entity.User;
import com.rebra.jwt.Token;
import com.rebra.jwt.TokenProvider;
import com.rebra.repository.RefreshTokenRepository;
import com.rebra.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private User testUser;
    private Token accessToken;
    private Token refreshToken;
    private RefreshToken refreshTokenEntity;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .sub("kakao-sub-123")
                .nickname("테스트사용자")
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L); // 필수!

        accessToken = new Token("access-token-value");
        refreshToken = new Token("refresh-token-value");

        refreshTokenEntity = RefreshToken.builder()
                .refreshToken("refresh-token-value")
                .user(testUser) // testUser id가 1L로 설정되어 있어야 함
                .expirationDate(LocalDateTime.now().plusDays(7))
                .build();
    }


    @Test
    @DisplayName("사용자 ID로 Access Token 생성 성공")
    void generateAccessToken_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(tokenProvider.generateAccessToken(testUser)).willReturn(accessToken);

        // when
        Token result = tokenService.generateAccessToken(1L);

        // then
        assertThat(result).isEqualTo(accessToken);
        verify(userRepository).findById(1L);
        verify(tokenProvider).generateAccessToken(testUser);
    }

    @Test
    @DisplayName("사용자 ID로 Refresh Token 생성 성공")
    void generateRefreshToken_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(tokenProvider.generateRefreshToken(testUser)).willReturn(refreshToken);

        // when
        Token result = tokenService.generateRefreshToken(1L);

        // then
        assertThat(result).isEqualTo(refreshToken);
        verify(userRepository).findById(1L);
        verify(tokenProvider).generateRefreshToken(testUser);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 토큰 생성 실패")
    void generateToken_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> tokenService.generateAccessToken(999L));
        assertThrows(RuntimeException.class, () -> tokenService.generateRefreshToken(999L));
    }

    @Test
    @DisplayName("RTR 토큰 갱신 성공")
    void refreshTokensWithRotation_Success() {
        // given
        String oldRefreshTokenValue = "old-refresh-token";
        Token newAccessToken = new Token("new-access-token");
        Token newRefreshToken = new Token("new-refresh-token");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(refreshTokenRepository.findByRefreshToken(oldRefreshTokenValue))
                .willReturn(Optional.of(refreshTokenEntity));
        given(tokenProvider.generateAccessToken(testUser)).willReturn(newAccessToken);
        given(tokenProvider.generateRefreshToken(testUser)).willReturn(newRefreshToken);
        given(tokenProvider.getRefreshTokenExpireMinutes()).willReturn(10080); // 7일

        // when
        TokenRefreshResponse result = tokenService.refreshTokensWithRotation(oldRefreshTokenValue);

        // then
        assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);
        
        verify(refreshTokenRepository).delete(refreshTokenEntity);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 갱신 실패")
    void refreshTokensWithRotation_ExpiredToken_ThrowsException() {
        // given
        String expiredTokenValue = "expired-refresh-token";
        RefreshToken expiredToken = RefreshToken.builder()
                .refreshToken(expiredTokenValue)
                .user(testUser)
                .expirationDate(LocalDateTime.now().minusDays(1)) // 이미 만료됨
                .build();
        
        given(refreshTokenRepository.findByRefreshToken(expiredTokenValue))
                .willReturn(Optional.of(expiredToken));

        // when & then
        assertThrows(RuntimeException.class, 
                () -> tokenService.refreshTokensWithRotation(expiredTokenValue));
    }

    @Test
    @DisplayName("존재하지 않는 Refresh Token으로 갱신 실패")
    void refreshTokensWithRotation_TokenNotFound_ThrowsException() {
        // given
        String nonExistentToken = "non-existent-token";
        given(refreshTokenRepository.findByRefreshToken(nonExistentToken))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, 
                () -> tokenService.refreshTokensWithRotation(nonExistentToken));
    }

    @Test
    @DisplayName("사용자의 모든 Refresh Token 삭제 성공")
    void deleteAllUserRefreshTokens_Success() {
        // given
        RefreshToken token1 = RefreshToken.builder().refreshToken("token1").user(testUser).build();
        RefreshToken token2 = RefreshToken.builder().refreshToken("token2").user(testUser).build();
        
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(refreshTokenRepository.findAllByUser(testUser)).willReturn(Arrays.asList(token1, token2));

        // when
        tokenService.deleteAllUserRefreshTokens(1L);

        // then
        verify(refreshTokenRepository).deleteAll(Arrays.asList(token1, token2));
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 사용자 ID 검증 성공")
    void validateRefreshTokenAndGetUserId_Success() {
        // given
        String validToken = "valid-refresh-token";
        given(tokenProvider.validateToken(validToken)).willReturn(true);
        given(refreshTokenRepository.findByRefreshToken(validToken))
                .willReturn(Optional.of(refreshTokenEntity));

        // when
        Long result = tokenService.validateRefreshTokenAndGetUserId(validToken);

        // then
        assertThat(result).isEqualTo(1L);
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 검증 실패")
    void validateRefreshTokenAndGetUserId_InvalidToken_ReturnsNull() {
        // given
        String invalidToken = "invalid-refresh-token";
        given(tokenProvider.validateToken(invalidToken)).willReturn(false);

        // when
        Long result = tokenService.validateRefreshTokenAndGetUserId(invalidToken);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("사용자용 새 토큰 발급 성공")
    void issueNewTokensForUser_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(tokenProvider.generateRefreshToken(testUser)).willReturn(refreshToken);
        given(tokenProvider.generateAccessToken(testUser)).willReturn(accessToken);
        given(tokenProvider.getRefreshTokenExpireMinutes()).willReturn(10080);
        given(refreshTokenRepository.findAllByUser(testUser)).willReturn(Arrays.asList());

        // when
        TokenRefreshResponse result = tokenService.issueNewTokensForUser(1L);

        // then
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
        
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
}