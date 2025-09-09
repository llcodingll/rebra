package com.rebra.dto.common;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorDetail {
    
    private String field;
    private String message;
}