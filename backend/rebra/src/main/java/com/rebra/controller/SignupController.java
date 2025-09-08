package com.rebra.controller;

import com.rebra.dto.request.SignupRequest;
import com.rebra.dto.response.SignupResponse;
import com.rebra.service.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class SignupController {

    private final SignupService signupService;

    @GetMapping("/nickname/check")
    public ResponseEntity<Boolean> checkNicknameAvailability(@RequestParam String nickname) {
        boolean isAvailable = signupService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(isAvailable);
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> completeSignup(
            @Valid @RequestBody SignupRequest signupRequest,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Long userId = extractUserIdFromAuthentication(authentication);
        SignupResponse response = signupService.completeSignup(userId, signupRequest);
        
        return ResponseEntity.ok(response);
    }

    private Long extractUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Long id) {
            return id;
        } else if (principal instanceof String str) {
            return Long.parseLong(str);
        } else {
            throw new IllegalArgumentException("지원하지 않는 principal 타입: " + principal.getClass());
        }
    }

}