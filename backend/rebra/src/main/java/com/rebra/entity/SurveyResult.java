package com.rebra.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveyResult {
    
    private Integer age;
    
    private String mainIncomeSource;
    
    private Integer investmentPurpose;
    
    private Integer investmentExperience;
    
    private Integer riskTolerance;
}