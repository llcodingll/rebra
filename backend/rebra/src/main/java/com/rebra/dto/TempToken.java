package com.rebra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TempToken {
    private String sub;           // 카카오 고유ID
    private LocalDateTime expireAt; // 30분 후 만료

    public TempToken(String sub) {
        this.sub = sub;
        this.expireAt = LocalDateTime.now().plusMinutes(30);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }
}