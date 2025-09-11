package com.rebra.service;

import com.rebra.dto.request.SignupRequest;
import com.rebra.dto.response.UserProfileResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {
    UserProfileResponse findById(Long userId);
    
    Long createUser(String socialId, SignupRequest signupRequest);
    
    String getUserNickname(Long userId);
}
