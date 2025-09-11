package com.rebra.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 사용자별 SALT 기반 계좌 정보 암호화 유틸리티
 */
@Slf4j
@Component
public class AccountEncryptionUtil {

    private static String hashPepper;
    
    @Value("${security.encryption.account.hash-pepper}")
    public void setHashPepper(String hashPepper) {
        AccountEncryptionUtil.hashPepper = hashPepper;
    }

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    // 필드 타입별 SALT 접미사
    private static final String ACCOUNT_NUMBER_SUFFIX = "rebra_salt_account";
    private static final String APP_KEY_SUFFIX = "rebra_salt_appkey";
    private static final String APP_SECRET_SUFFIX = "rebra_salt_appsecret";

    /**
     * 계좌번호 암호화
     */
    public static String encryptAccountNumber(String accountNumber, Long userId) {
        return encrypt(accountNumber, userId, ACCOUNT_NUMBER_SUFFIX);
    }

    /**
     * 계좌번호 복호화
     */
    public static String decryptAccountNumber(String encryptedData, Long userId) {
        return decrypt(encryptedData, userId, ACCOUNT_NUMBER_SUFFIX);
    }

    /**
     * 앱키 암호화
     */
    public static String encryptAppKey(String appKey, Long userId) {
        return encrypt(appKey, userId, APP_KEY_SUFFIX);
    }

    /**
     * 앱키 복호화
     */
    public static String decryptAppKey(String encryptedData, Long userId) {
        return decrypt(encryptedData, userId, APP_KEY_SUFFIX);
    }

    /**
     * 앱시크릿 암호화
     */
    public static String encryptAppSecret(String appSecret, Long userId) {
        return encrypt(appSecret, userId, APP_SECRET_SUFFIX);
    }

    /**
     * 앱시크릿 복호화
     */
    public static String decryptAppSecret(String encryptedData, Long userId) {
        return decrypt(encryptedData, userId, APP_SECRET_SUFFIX);
    }

    /**
     * 데이터 암호화
     */
    public static String encrypt(String data, Long userId, String fieldTypeSuffix) {
        try {
            if (data == null || data.trim().isEmpty()) {
                return data;
            }

            // 사용자별 SALT 생성
            String salt = generateSalt(userId, fieldTypeSuffix);
            
            // SALT 기반 암호화 키 생성
            SecretKey secretKey = generateKey(salt);
            
            // IV 생성
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            // 암호화 수행
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // IV + 암호화된 데이터를 Base64로 인코딩
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            log.error("암호화 실패: userId={}, fieldType={}", userId, fieldTypeSuffix, e);
            throw new RuntimeException("데이터 암호화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 데이터 복호화
     */
    public static String decrypt(String encryptedData, Long userId, String fieldTypeSuffix) {
        try {
            if (encryptedData == null || encryptedData.trim().isEmpty()) {
                return encryptedData;
            }

            // Base64 디코딩
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            
            // IV 추출
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decodedData, 0, iv, 0, GCM_IV_LENGTH);
            
            // 암호화된 데이터 추출
            byte[] encrypted = new byte[decodedData.length - GCM_IV_LENGTH];
            System.arraycopy(decodedData, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            // 사용자별 SALT 생성
            String salt = generateSalt(userId, fieldTypeSuffix);
            
            // SALT 기반 암호화 키 생성
            SecretKey secretKey = generateKey(salt);
            
            // 복호화 수행
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] decryptedData = cipher.doFinal(encrypted);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("복호화 실패: userId={}, fieldType={}", userId, fieldTypeSuffix, e);
            throw new RuntimeException("데이터 복호화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자별 SALT 생성
     */
    private static String generateSalt(Long userId, String fieldTypeSuffix) {
        return userId + fieldTypeSuffix;
    }

    /**
     * SALT 기반 암호화 키 생성
     */
    private static SecretKey generateKey(String salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hash = digest.digest(salt.getBytes(StandardCharsets.UTF_8));
        
        // AES 키 생성을 위해 해시를 적절한 크기로 자르기
        byte[] keyBytes = new byte[KEY_LENGTH / 8]; // 256비트 = 32바이트
        System.arraycopy(hash, 0, keyBytes, 0, Math.min(hash.length, keyBytes.length));
        
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 계좌번호 해시 생성 (중복 확인용)
     */
    public static String generateAccountNumberHash(String plainAccountNumber) {
        try {
            if (plainAccountNumber == null || plainAccountNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("계좌번호는 null이거나 빈 값일 수 없습니다.");
            }
            
            // 계좌번호 + Pepper로 해시 생성
            String dataWithPepper = plainAccountNumber + hashPepper;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataWithPepper.getBytes(StandardCharsets.UTF_8));
            
            // 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            log.error("계좌번호 해시 생성 실패: {}", plainAccountNumber, e);
            throw new RuntimeException("계좌번호 해시 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 민감정보 마스킹
     */
    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        return accountNumber.substring(0, 4) + "****";
    }

    /**
     * 앱키 마스킹
     */
    public static String maskAppKey(String appKey) {
        if (appKey == null || appKey.length() <= 4) {
            return "****";
        }
        return appKey.substring(0, 4) + "****";
    }

    /**
     * 앱시크릿 완전 마스킹
     */
    public static String maskAppSecret(String appSecret) {
        return "****";
    }
}