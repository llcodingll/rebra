package com.rebra.service;

import com.rebra.dto.DecryptedAccountCredentials;
import com.rebra.dto.request.AccountReconnectRequest;
import com.rebra.dto.request.AccountRegisterRequest;
import com.rebra.dto.request.AccountVerifyRequest;
import com.rebra.dto.response.AccountDetailResponse;
import com.rebra.dto.response.AccountListResponse;
import com.rebra.dto.response.AccountRegisterResponse;
import com.rebra.dto.response.AccountVerifyResponse;
import com.rebra.component.KisApiComponent;
import com.rebra.entity.Account;
import com.rebra.entity.ConnectionStatus;
import com.rebra.entity.User;
import com.rebra.exception.account.AccountException;
import com.rebra.exception.user.UserException;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.UserRepository;
import com.rebra.util.AccountConverter;
import com.rebra.util.AccountEncryptionUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final KisApiComponent kisApiComponent;

    /**
     * 계좌 등록 시 인증
     * @param request
     * @return
     */
    @Override
    public AccountVerifyResponse verifyAccount(AccountVerifyRequest request) {
        try {
            log.info("계좌 인증 시작 - 계좌번호: {}", request.getAccountNumber());

            kisApiComponent.verifyAccount(request.getAccountNumber(), request.getAppKey(), request.getAppSecret(), request.getAccountType());

            log.info("계좌 인증 성공 - 계좌번호: {}, 계좌타입: {}", 
                request.getAccountNumber());

            return AccountVerifyResponse.success(request.getAccountType());

        } catch (Exception e) {
            log.error("계좌 인증 실패 - 계좌번호: {}, 오류: {}", 
                request.getAccountNumber(), e.getMessage(), e);
            throw AccountException.accountVerificationFailed();
        }
    }

    @Override
    @Transactional
    public AccountRegisterResponse registerAccount(Long userId, AccountRegisterRequest request) {
        log.info("계좌 등록 시작 - 사용자ID: {}, 계좌번호: {}", userId, request.getAccountNumber());

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
            .orElseThrow(() -> UserException.userNotFound());

        String accountNumberHash = AccountEncryptionUtil.generateAccountNumberHash(request.getAccountNumber());
        Optional<Account> existing = accountRepository.findByAccountNumberHash(accountNumberHash);

        if(existing.isPresent()) {
            if(!existing.get().getIsDeleted()) {
                throw AccountException.duplicateAccount();
            }

            existing.get().create();

            kisApiComponent.verifyAccount(request.getAccountNumber(), request.getAppKey(), request.getAppSecret(), request.getAccountType());

            kisApiComponent.addUserCredentials(
                    userId,
                    existing.get().getId(),
                    request.getAccountNumber(),
                    request.getAppKey(),
                    request.getAppSecret(),
                    request.getAccountType()
            );

            return AccountRegisterResponse.success(
                    existing.get().getId(),
                    AccountEncryptionUtil.maskAccountNumber(request.getAccountNumber()),
                    existing.get().getAccountType(),
                    existing.get().getCreatedAt()
            );
        }

        try {
            // KIS API 재검증
            kisApiComponent.verifyAccount(request.getAccountNumber(), request.getAppKey(), request.getAppSecret(), request.getAccountType());

            // 계좌 정보 암호화 및 저장
            Account account = AccountConverter.fromRegisterRequest(user, request, request.getAccountType());
            accountRepository.save(account);
            
            // KIS API Component에 사용자 Credentials 등록
            kisApiComponent.addUserCredentials(
                userId, 
                account.getId(), 
                request.getAccountNumber(),
                request.getAppKey(),
                request.getAppSecret(),
                request.getAccountType()
            );

            log.info("계좌 등록 성공 - 사용자ID: {}, 계좌ID: {}", userId, account.getId());

            return AccountRegisterResponse.success(
                account.getId(),
                AccountEncryptionUtil.maskAccountNumber(request.getAccountNumber()),
                account.getAccountType(),
                account.getCreatedAt()
            );

        } catch (Exception e) {
            log.error("계좌 등록 실패 - 사용자ID: {}, 오류: {}", userId, e.getMessage(), e);
            throw AccountException.accountRegistrationFailed();
        }
    }

    /**
     * 계좌 목록 조회 (이미 포트폴리오와 연관되어있는 계좌는 조회되지 않습니다. 현재 연관 되어 있는 계좌 빼고 조회하는 건 X)
     * @param userId
     * @return
     */
    @Override
    public AccountListResponse getAccountList(Long userId) {
        log.info("계좌 목록 조회 - 사용자ID: {}", userId);

        List<Account> accounts = accountRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);

        List<AccountListResponse.AccountSummary> accountSummaries = accounts.stream()
            .map(AccountConverter::toAccountSummary)
            .collect(Collectors.toList());

        return AccountListResponse.of(accountSummaries);
    }

    /**
     * 계좌 단건 조회 (이미 포트폴리오와 연관되어있는 계좌는 조회되지 않습니다. 현재 연관 되어 있는 계좌 빼고 조회하는 건 X)
     * @param userId
     * @param accountId
     * @return
     */
    @Override
    public AccountDetailResponse getAccountDetail(Long userId, Long accountId) {
        log.info("계좌 상세 조회 - 사용자ID: {}, 계좌ID: {}", userId, accountId);

        Account account = accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
            .orElseThrow(() -> AccountException.accountNotFound());

        return AccountConverter.toAccountDetail(account);
    }

    /**
     * 계좌 삭제
     * @param userId
     * @param accountId
     */
    @Override
    @Transactional
    public void deleteAccount(Long userId, Long accountId) {
        log.info("계좌 삭제 시작 - 사용자ID: {}, 계좌ID: {}", userId, accountId);

        Account account = accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
            .orElseThrow(() -> AccountException.accountNotFound());

        // 계좌 삭제 (soft delete)
        account.delete();
        
        // KIS API Component에서 사용자 Credentials 제거
        kisApiComponent.removeUserCredentials(userId, accountId, account.getAccountType());

        log.info("계좌 삭제 완료 - 사용자ID: {}, 계좌ID: {}", userId, accountId);
    }


    /**
     * 계좌 중복 확인 (해시 기반)
     */
//    private void validateAccountDuplication(String accountNumber) {
//        String accountNumberHash = AccountEncryptionUtil.generateAccountNumberHash(accountNumber);
//        if (accountRepository.existsByAccountNumberHash(accountNumberHash)) {
//            throw AccountException.duplicateAccount();
//        }
//    }

    /**
     * 사용자별 계좌 조회 (공통 로직)
     */
    private Account findAccountByUserAndId(Long userId, Long accountId) {
        return accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
            .orElseThrow(() -> AccountException.accountNotFound());
    }

    /**
     * 연결 상태 업데이트 및 저장 (공통 로직)
     */
    private void updateConnectionStatusAndSave(Account account, ConnectionStatus status) {
        account.updateConnectionStatus(status);
    }

    /**
     * 계좌 재연결 시 필요한 로직, 지금은 사용하지 않습니다.
     * 받아야 할 것: 유저ID, 계좌ID, DTO(앱키, 앱시크릿키, 계좌타입)
     * @param userId
     * @param reconnectRequest
     * @return
     */
    @Override
    public AccountVerifyResponse verifyAccountReconnection(Long userId, AccountReconnectRequest reconnectRequest) {

        Account account = findAccountByUserAndId(userId, reconnectRequest.getAccountId());

        try {
            DecryptedAccountCredentials credentials = AccountEncryptionUtil.decryptAccountCredentials(account, userId);

            kisApiComponent.ensureUserCredentials(userId, account.getId(), account.getAccountType(), credentials);

            kisApiComponent.verifyAccount(credentials, account.getAccountType());

            updateConnectionStatusAndSave(account, ConnectionStatus.CONNECTED);
            return AccountVerifyResponse.success(account.getAccountType());

        } catch (Exception e) {
            updateConnectionStatusAndSave(account, ConnectionStatus.FAILED);
            throw AccountException.kisConnectionFailed();
        }
    }
}