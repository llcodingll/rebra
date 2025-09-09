package com.rebra.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    private String nickname;
    
    @NotNull(message = "나이는 필수입니다.")
    @Positive(message = "나이는 양수여야 합니다.")
    private Integer age;
    
    @NotBlank(message = "주요 소득원은 필수입니다.")
    private String mainIncomeSource;
    
    @NotNull(message = "투자 목적은 필수입니다.")
    @Min(value = 10, message = "투자 목적 점수는 최소 10점입니다.")
    @Max(value = 45, message = "투자 목적 점수는 최대 45점입니다.")
    private Integer investmentPurpose;
    
    @NotNull(message = "투자 경험은 필수입니다.")
    @Min(value = 2, message = "투자 경험 점수는 최소 2점입니다.")
    @Max(value = 58, message = "투자 경험 점수는 최대 58점입니다.")
    private Integer investmentExperience;
    
    @NotNull(message = "위험 감수성은 필수입니다.")
    @Min(value = 5, message = "위험 감수성 점수는 최소 5점입니다.")
    @Max(value = 45, message = "위험 감수성 점수는 최대 45점입니다.")
    private Integer riskTolerance;

}