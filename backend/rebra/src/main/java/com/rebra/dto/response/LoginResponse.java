package com.rebra.dto.response;

import com.rebra.jwt.Token;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Token accessToken;
    private Token refreshToken;
    private String nickname;
}