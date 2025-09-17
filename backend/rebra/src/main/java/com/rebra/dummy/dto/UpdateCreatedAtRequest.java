package com.rebra.dummy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCreatedAtRequest {

    @NotNull(message = "생성일시는 필수입니다")
    private LocalDateTime createdAt;
}