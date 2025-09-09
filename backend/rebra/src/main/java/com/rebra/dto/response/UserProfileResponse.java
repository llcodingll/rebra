package com.rebra.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String nickname;
}
