package com.rebra.dto.request;

import com.rebra.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 계좌 재연결 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountReconnectRequest {

    @NotNull
    private Long accountId;

    @NotBlank(message = "앱키는 필수입니다")
    private String appKey;

    @NotBlank(message = "앱시크릿은 필수입니다")
    private String appSecret;

    private AccountType accountType;
}