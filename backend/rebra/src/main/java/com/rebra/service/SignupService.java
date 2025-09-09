package com.rebra.service;

import com.rebra.dto.request.SignupRequest;
import com.rebra.dto.response.SignupResponse;

public interface SignupService {

    boolean isNicknameAvailable(String nickname);
    
    SignupResponse completeSignup(Long userId, SignupRequest signupRequest);

}