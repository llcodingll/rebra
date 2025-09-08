package com.rebra.dto.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidationErrorResponse {
    
    private List<ValidationErrorDetail> errors;
}