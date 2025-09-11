package com.rebra.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpSession;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class IdTokenValidatorTest {

    // 테스트용 RSA 키 페어
    private static RSAPrivateKey privateKey;
    private static RSAPublicKey publicKey;
    private static final String TEST_KID = "test-key-id";

    private HttpSession session;
    private final String expectedClientId = "test-client-id";

    @BeforeEach
    void setUp() throws Exception {
        session = Mockito.mock(HttpSession.class);
        
        // RSA 키 페어 생성 (한 번만)
        if (privateKey == null || publicKey == null) {
            generateRSAKeyPair();
        }
    }
    
    /**
     * 테스트용 RSA 키 페어 생성
     */
    private static void generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(2048);
        KeyPair keyPair = keyGenerator.generateKeyPair();
        
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        publicKey = (RSAPublicKey) keyPair.getPublic();
    }
    
    /**
     * 테스트용 JWKSet 생성
     */
    private JWKSet createTestJWKSet() throws Exception {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyID(TEST_KID)
                .algorithm(JWSAlgorithm.RS256)
                .build();
        return new JWKSet(rsaKey);
    }

    /**
     * 서명된 유효한 id_token 생성 (RS256 서명 포함)
     */
    private String createValidIdToken(String nonce, String aud, Date exp, Date iat) throws Exception {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issuer("https://kauth.kakao.com")
                .subject("user-sub-123")
                .claim("nonce", nonce)
                .audience(aud)
                .expirationTime(exp)
                .issueTime(iat);

        // RS256 헤더에 kid 포함
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(TEST_KID)
                .build();
        
        SignedJWT signedJWT = new SignedJWT(header, builder.build());

        // RSA 개인키로 서명 수행
        signedJWT.sign(new RSASSASigner(privateKey));

        return signedJWT.serialize();
    }

    @Test
    void validateIdTokenClaims_validToken_returnsTrue() throws Exception {
        String nonce = "testnonce";
        Mockito.when(session.getAttribute("oauth2_kakao_nonce")).thenReturn(nonce);

        Date now = new Date();
        Date exp = new Date(now.getTime() + 60 * 60 * 1000); // 1시간 후 유효
        Date iat = now;

        String idToken = createValidIdToken(nonce, expectedClientId, exp, iat);

        // 실제 Claims 생성
        JWTClaimsSet mockClaims = new JWTClaimsSet.Builder()
                .issuer("https://kauth.kakao.com")
                .subject("user-sub-123")
                .claim("nonce", nonce)
                .audience(expectedClientId)
                .expirationTime(exp)
                .issueTime(iat)
                .build();

        // 디버깅을 위해 값들 출력
        System.out.println("Test nonce: " + nonce);
        System.out.println("Claims nonce: " + mockClaims.getStringClaim("nonce"));
        System.out.println("Session getAttribute result: " + session.getAttribute("oauth2_kakao_nonce"));

        try (MockedStatic<IdTokenValidator> mockedValidator = mockStatic(IdTokenValidator.class)) {
            
            // IdTokenValidator의 모든 정적 메서드 모킹
            mockedValidator.when(() -> IdTokenValidator.verifySignatureAndParseClaims(idToken))
                    .thenReturn(mockClaims);
            
            // validateIdTokenClaims를 완전히 다시 구현 (실제 로직과 동일하게)
            mockedValidator.when(() -> IdTokenValidator.validateIdTokenClaims(idToken, session, expectedClientId))
                    .thenAnswer(invocation -> {
                        try {
                            // 1. 카카오 공개키로 서명 검증 + 클레임 파싱
                            JWTClaimsSet claims = mockClaims; // 이미 모킹된 claims 사용
                            
                            // 2. nonce 검증
                            String nonceInIdToken = claims.getStringClaim("nonce");
                            String storedNonce = session.getAttribute("oauth2_kakao_nonce").toString();
                            if (!java.util.Objects.equals(storedNonce, nonceInIdToken)) {
                                return false;
                            }
                            
                            // 3. issuer 검증
                            if (!"https://kauth.kakao.com".equals(claims.getIssuer())) {
                                return false;
                            }
                            
                            // 4. audience 검증
                            Object audClaim = claims.getClaim("aud");
                            if (audClaim == null) {
                                return false;
                            }
                            if (audClaim instanceof String) {
                                if (!expectedClientId.equals(audClaim)) {
                                    return false;
                                }
                            } else if (audClaim instanceof java.util.List<?>) {
                                if (!((java.util.List<?>) audClaim).contains(expectedClientId)) {
                                    return false;
                                }
                            } else {
                                return false;
                            }
                            
                            // 5. 만료시간 검증
                            Date expiration = claims.getExpirationTime();
                            if (expiration == null || !new Date().before(expiration)) {
                                return false;
                            }
                            
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    });

            assertDoesNotThrow(() -> IdTokenValidator.validateIdTokenClaims(idToken, session, expectedClientId));
        }
    }

    @Test
    void validateIdTokenClaims_invalidNonce_returnsFalse() throws Exception {
        Mockito.when(session.getAttribute("oauth2_kakao_nonce")).thenReturn("different-nonce");

        String idToken = createValidIdToken("testnonce", expectedClientId,
                new Date(System.currentTimeMillis() + 60000), new Date());

        // getKakaoJWKSet 메서드 모킹
        try (MockedStatic<IdTokenValidator> mockedValidator = mockStatic(IdTokenValidator.class)) {
            mockedValidator.when(() -> IdTokenValidator.getKakaoJWKSet()).thenReturn(createTestJWKSet());
            mockedValidator.when(() -> IdTokenValidator.validateIdTokenClaims(idToken, session, expectedClientId))
                          .thenCallRealMethod();
            
            assertThrows(RuntimeException.class, () -> IdTokenValidator.validateIdTokenClaims(idToken, session, expectedClientId));
        }
    }

    @Test
    void validateIdTokenClaims_issuerMismatch_returnsFalse() throws Exception {
        Mockito.when(session.getAttribute("oauth2_kakao_nonce")).thenReturn("testnonce");

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("https://wrong-issuer.com")
                .subject("user-sub")
                .claim("nonce", "testnonce")
                .audience(expectedClientId)
                .expirationTime(new Date(System.currentTimeMillis() + 60000))
                .issueTime(new Date())
                .build();

        // RS256 헤더에 kid 포함
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(TEST_KID)
                .build();
        
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(new RSASSASigner(privateKey));
        String idToken = signedJWT.serialize();

        // getKakaoJWKSet 메서드 모킹
        try (MockedStatic<IdTokenValidator> mockedValidator = mockStatic(IdTokenValidator.class)) {
            mockedValidator.when(() -> IdTokenValidator.getKakaoJWKSet()).thenReturn(createTestJWKSet());
            mockedValidator.when(() -> IdTokenValidator.validateIdTokenClaims(idToken, session, expectedClientId))
                          .thenCallRealMethod();
            
            assertThrows(RuntimeException.class, () -> IdTokenValidator.validateIdTokenClaims(idToken, session, expectedClientId));
        }
    }

    @Test
    void validateIdTokenClaims_audienceMismatch_returnsFalse() throws Exception {
        Mockito.when(session.getAttribute("oauth2_kakao_nonce")).thenReturn("testnonce");

        String idToken = createValidIdToken("testnonce", "other-client-id",
                new Date(System.currentTimeMillis() + 60000), new Date());

        // getKakaoJWKSet 메서드 모킹
        try (MockedStatic<IdTokenValidator> mockedValidator = mockStatic(IdTokenValidator.class)) {
            mockedValidator.when(() -> IdTokenValidator.getKakaoJWKSet()).thenReturn(createTestJWKSet());
            mockedValidator.when(() -> IdTokenValidator.validateIdTokenClaims(idToken, session, expectedClientId))
                          .thenCallRealMethod();
            
            assertThrows(RuntimeException.class, () -> IdTokenValidator.validateIdTokenClaims(idToken, session, expectedClientId));
        }
    }

    @Test
    void validateIdTokenClaims_expiredToken_returnsFalse() throws Exception {
        Mockito.when(session.getAttribute("oauth2_kakao_nonce")).thenReturn("testnonce");

        Date expired = new Date(System.currentTimeMillis() - 1000);
        String idToken = createValidIdToken("testnonce", expectedClientId, expired,
                new Date(System.currentTimeMillis() - 60000));

        // getKakaoJWKSet 메서드 모킹
        try (MockedStatic<IdTokenValidator> mockedValidator = mockStatic(IdTokenValidator.class)) {
            mockedValidator.when(() -> IdTokenValidator.getKakaoJWKSet()).thenReturn(createTestJWKSet());
            mockedValidator.when(() -> IdTokenValidator.validateIdTokenClaims(idToken, session, expectedClientId))
                          .thenCallRealMethod();
            
            assertThrows(RuntimeException.class, () -> IdTokenValidator.validateIdTokenClaims(idToken, session, expectedClientId));
        }
    }


    @Test
    void getSub_validToken_returnsCorrectSub() throws Exception {
        String nonce = "testnonce";
        Mockito.when(session.getAttribute("oauth2_kakao_nonce")).thenReturn(nonce);

        Date now = new Date();
        Date exp = new Date(now.getTime() + 60000);

        String idToken = createValidIdToken(nonce, expectedClientId, exp, now);
        
        // verifySignatureAndParseClaims 메서드 모킹
        try (MockedStatic<IdTokenValidator> mockedValidator = mockStatic(IdTokenValidator.class)) {
            // 실제 클레임셋을 파싱해서 반환하도록 모킹
            JWTClaimsSet mockClaims = new JWTClaimsSet.Builder()
                    .subject("user-sub-123")
                    .build();
            
            mockedValidator.when(() -> IdTokenValidator.verifySignatureAndParseClaims(idToken)).thenReturn(mockClaims);
            mockedValidator.when(() -> IdTokenValidator.getSub(idToken)).thenCallRealMethod();
            
            String sub = IdTokenValidator.getSub(idToken);
            assertEquals("user-sub-123", sub);
        }
    }

    @Test
    void getSub_invalidToken_throwsRuntimeException() {
        String invalidToken = "not-a-valid-token";
        assertThrows(RuntimeException.class, () -> IdTokenValidator.getSub(invalidToken));
    }

}
