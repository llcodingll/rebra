package com.rebra.dto.common;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {
    
    private List<ValidationErrorDetail> errors;
}