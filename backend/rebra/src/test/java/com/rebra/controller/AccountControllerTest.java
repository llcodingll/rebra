package com.rebra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebra.config.SecurityTestConfig;
import com.rebra.dto.request.AccountRegisterRequest;
import com.rebra.dto.request.AccountVerifyRequest;
import com.rebra.dto.response.AccountDetailResponse;
import com.rebra.dto.response.AccountListResponse;
import com.rebra.dto.response.AccountRegisterResponse;
import com.rebra.dto.response.AccountVerifyResponse;
import com.rebra.entity.AccountType;
import com.rebra.exception.account.AccountException;
import com.rebra.exception.user.UserException;
import com.rebra.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(SecurityTestConfig.class)
@DisplayName("AccountController 테스트")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;


    @Nested
    @DisplayName("계좌 인증 API 테스트")
    class VerifyAccountTest {

        @Test
        @DisplayName("성공 - 계좌 인증 성공")
        void verifyAccount_성공() throws Exception {
            // Given
            AccountVerifyRequest request = new AccountVerifyRequest(
                    "12345678",
                    "test-app-key-12345678901234567890",
                    "test-app-secret-12345678901234567890123456789012345678901234567890",
                    AccountType.MOCK
            );
            AccountVerifyResponse response = AccountVerifyResponse.success(AccountType.MOCK);

            given(accountService.verifyAccount(any(AccountVerifyRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/accounts/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.connected").value(true))
                    .andExpect(jsonPath("$.data.accountType").value("MOCK"))
                    .andExpect(jsonPath("$.data.brokerName").value("한국투자증권"));

            verify(accountService).verifyAccount(any(AccountVerifyRequest.class));
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 데이터 (계좌번호 짧음)")
        void verifyAccount_실패_잘못된계좌번호() throws Exception {
            // Given
            AccountVerifyRequest invalidRequest = new AccountVerifyRequest(
                    "123", // 8자 미만
                    "test-app-key-12345678901234567890",
                    "test-app-secret-12345678901234567890123456789012345678901234567890",
                    AccountType.MOCK
            );

            // When & Then
            mockMvc.perform(post("/api/v1/accounts/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(accountService);
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 데이터 (앱키 짧음)")
        void verifyAccount_실패_잘못된앱키() throws Exception {
            // Given
            AccountVerifyRequest invalidRequest = new AccountVerifyRequest(
                    "12345678",
                    "short", // 20자 미만
                    "test-app-secret-12345678901234567890123456789012345678901234567890",
                    AccountType.MOCK
            );

            // When & Then
            mockMvc.perform(post("/api/v1/accounts/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(accountService);
        }

        @Test
        @DisplayName("실패 - KIS API 연결 실패")
        void verifyAccount_실패_KIS연결실패() throws Exception {
            // Given
            AccountVerifyRequest request = new AccountVerifyRequest(
                    "12345678",
                    "test-app-key-12345678901234567890",
                    "test-app-secret-12345678901234567890123456789012345678901234567890",
                    AccountType.MOCK
            );

            given(accountService.verifyAccount(any(AccountVerifyRequest.class)))
                    .willThrow(AccountException.accountVerificationFailed());

            // When & Then
            mockMvc.perform(post("/api/v1/accounts/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(accountService).verifyAccount(any(AccountVerifyRequest.class));
        }
    }

    @Nested
    @DisplayName("계좌 등록 API 테스트")
    class RegisterAccountTest {

        @Test
        @DisplayName("성공 - 계좌 등록 성공")
        void registerAccount_성공() throws Exception {
            // Given
            Long userId = 1L;
            AccountRegisterRequest request = new AccountRegisterRequest(
                    "12345678",
                    "test-app-key-12345678901234567890",
                    "test-app-secret-12345678901234567890123456789012345678901234567890",
                    AccountType.MOCK
            );
            AccountRegisterResponse response = AccountRegisterResponse.success(
                    1L, "123-45-***01", AccountType.MOCK, null
            );

            given(accountService.registerAccount(eq(userId), any(AccountRegisterRequest.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/accounts/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.accountId").value(1L))
                    .andExpect(jsonPath("$.data.accountNumber").value("123-45-***01"))
                    .andExpect(jsonPath("$.data.accountType").value("MOCK"));

            verify(accountService).registerAccount(eq(userId), any(AccountRegisterRequest.class));
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 데이터")
        void registerAccount_실패_잘못된요청() throws Exception {
            // Given
            AccountRegisterRequest invalidRequest = new AccountRegisterRequest(
                    "", // 빈 계좌번호
                    "test-app-key-12345678901234567890",
                    "test-app-secret-12345678901234567890123456789012345678901234567890",
                    AccountType.MOCK
            );

            // When & Then
            mockMvc.perform(post("/api/v1/accounts/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(accountService);
        }

        @Test
        @DisplayName("실패 - 중복 계좌 등록")
        void registerAccount_실패_중복계좌() throws Exception {
            // Given
            Long userId = 1L;
            AccountRegisterRequest request = new AccountRegisterRequest(
                    "12345678",
                    "test-app-key-12345678901234567890",
                    "test-app-secret-12345678901234567890123456789012345678901234567890",
                    AccountType.MOCK
            );

            given(accountService.registerAccount(eq(userId), any(AccountRegisterRequest.class)))
                    .willThrow(AccountException.duplicateAccount());

            // When & Then
            mockMvc.perform(post("/api/v1/accounts/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            verify(accountService).registerAccount(eq(userId), any(AccountRegisterRequest.class));
        }

        @Test
        @DisplayName("실패 - 사용자를 찾을 수 없음")
        void registerAccount_실패_사용자없음() throws Exception {
            // Given - SecurityTestConfig provides userId = 1L
            Long userId = 1L;
            AccountRegisterRequest request = new AccountRegisterRequest(
                    "12345678",
                    "test-app-key-12345678901234567890",
                    "test-app-secret-12345678901234567890123456789012345678901234567890",
                    AccountType.MOCK
            );

            given(accountService.registerAccount(eq(userId), any(AccountRegisterRequest.class)))
                    .willThrow(UserException.userNotFound());

            // When & Then
            mockMvc.perform(post("/api/v1/accounts/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(accountService).registerAccount(eq(userId), any(AccountRegisterRequest.class));
        }
    }

    @Nested
    @DisplayName("계좌 목록 조회 API 테스트")
    class GetAccountListTest {

        @Test
        @DisplayName("성공 - 계좌 목록 조회 성공")
        void getAccountList_성공() throws Exception {
            // Given
            Long userId = 1L;
            AccountListResponse.AccountSummary accountSummary = new AccountListResponse.AccountSummary(
                    1L, "123-45-***01", AccountType.MOCK, "한국투자증권", true, null
            );
            AccountListResponse response = AccountListResponse.of(Arrays.asList(accountSummary));

            given(accountService.getAccountList(userId)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.totalCount").value(1))
                    .andExpect(jsonPath("$.data.accounts").isArray())
                    .andExpect(jsonPath("$.data.accounts[0].accountId").value(1L))
                    .andExpect(jsonPath("$.data.accounts[0].accountType").value("MOCK"));

            verify(accountService).getAccountList(userId);
        }

        @Test
        @DisplayName("성공 - 빈 계좌 목록")
        void getAccountList_빈목록() throws Exception {
            // Given
            Long userId = 1L;
            AccountListResponse response = AccountListResponse.of(Collections.emptyList());

            given(accountService.getAccountList(userId)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.totalCount").value(0))
                    .andExpect(jsonPath("$.data.accounts").isArray())
                    .andExpect(jsonPath("$.data.accounts").isEmpty());

            verify(accountService).getAccountList(userId);
        }
    }

    @Nested
    @DisplayName("계좌 상세 조회 API 테스트")
    class GetAccountDetailTest {

        @Test
        @DisplayName("성공 - 계좌 상세 조회 성공")
        void getAccountDetail_성공() throws Exception {
            // Given
            Long userId = 1L;
            Long accountId = 1L;
            AccountDetailResponse response = AccountDetailResponse.of(
                    accountId, "123-45-***01", "***", AccountType.MOCK, "한국투자증권",
                    true, null, "정상", true
            );

            given(accountService.getAccountDetail(userId, accountId)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/accounts/{accountId}", accountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.accountId").value(accountId))
                    .andExpect(jsonPath("$.data.accountNumber").value("123-45-***01"))
                    .andExpect(jsonPath("$.data.accountType").value("MOCK"))
                    .andExpect(jsonPath("$.data.brokerName").value("한국투자증권"))
                    .andExpect(jsonPath("$.data.connected").value(true));

            verify(accountService).getAccountDetail(userId, accountId);
        }

        @Test
        @DisplayName("실패 - 계좌를 찾을 수 없음")
        void getAccountDetail_실패_계좌없음() throws Exception {
            // Given
            Long userId = 1L;
            Long accountId = 999L;

            given(accountService.getAccountDetail(userId, accountId))
                    .willThrow(AccountException.accountNotFound());

            // When & Then
            mockMvc.perform(get("/api/v1/accounts/{accountId}", accountId))
                    .andExpect(status().isNotFound());

            verify(accountService).getAccountDetail(userId, accountId);
        }

        @Test
        @DisplayName("실패 - 접근 권한 없음 (다른 사용자의 계좌)")
        void getAccountDetail_실패_권한없음() throws Exception {
            // Given
            Long userId = 1L;
            Long accountId = 1L;

            given(accountService.getAccountDetail(userId, accountId))
                    .willThrow(AccountException.accountAccessDenied());

            // When & Then
            mockMvc.perform(get("/api/v1/accounts/{accountId}", accountId))
                    .andExpect(status().isForbidden());

            verify(accountService).getAccountDetail(userId, accountId);
        }
    }

    @Nested
    @DisplayName("계좌 삭제 API 테스트")
    class DeleteAccountTest {

        @Test
        @DisplayName("성공 - 계좌 삭제 성공")
        void deleteAccount_성공() throws Exception {
            // Given
            Long userId = 1L;
            Long accountId = 1L;

            doNothing().when(accountService).deleteAccount(userId, accountId);

            // When & Then
            mockMvc.perform(delete("/api/v1/accounts/{accountId}", accountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200));

            verify(accountService).deleteAccount(userId, accountId);
        }

        @Test
        @DisplayName("실패 - 계좌를 찾을 수 없음")
        void deleteAccount_실패_계좌없음() throws Exception {
            // Given
            Long userId = 1L;
            Long accountId = 999L;

            willThrow(AccountException.accountNotFound())
                    .given(accountService).deleteAccount(userId, accountId);

            // When & Then
            mockMvc.perform(delete("/api/v1/accounts/{accountId}", accountId))
                    .andExpect(status().isNotFound());

            verify(accountService).deleteAccount(userId, accountId);
        }

        @Test
        @DisplayName("실패 - 삭제 권한 없음 (다른 사용자의 계좌)")
        void deleteAccount_실패_권한없음() throws Exception {
            // Given
            Long userId = 1L;
            Long accountId = 1L;

            willThrow(AccountException.accountAccessDenied())
                    .given(accountService).deleteAccount(userId, accountId);

            // When & Then
            mockMvc.perform(delete("/api/v1/accounts/{accountId}", accountId))
                    .andExpect(status().isForbidden());

            verify(accountService).deleteAccount(userId, accountId);
        }
    }
}