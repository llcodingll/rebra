package com.rebra.dto.request;

import com.rebra.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountVerifyRequest {

    @NotBlank(message = "계좌번호는 필수입니다.")
    @Size(min = 8, max = 15, message = "계좌번호는 8자 이상 15자 이하여야 합니다.")
    private String accountNumber;

    @NotBlank(message = "앱키는 필수입니다.")
    @Size(min = 20, max = 50, message = "앱키는 20자 이상 50자 이하여야 합니다.")
    private String appKey;

    @NotBlank(message = "앱시크릿은 필수입니다.")
    @Size(min = 50, max = 200, message = "앱시크릿은 50자 이상 200자 이하여야 합니다.")
    private String appSecret;

    private AccountType accountType;
}