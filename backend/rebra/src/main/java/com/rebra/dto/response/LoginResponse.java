package com.rebra.dto.response;

import com.rebra.jwt.Token;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Token refreshToken;
    private String nickname;
    private boolean signupRequired;
}