package com.rebra.controller;

import com.rebra.annotation.LoginUser;
import com.rebra.common.CommonApiResponse;
import com.rebra.dto.response.UserProfileResponse;
import com.rebra.entity.User;
import com.rebra.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<CommonApiResponse<UserProfileResponse>> getUserInfo(@LoginUser Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(CommonApiResponse.ok(new UserProfileResponse(user.getId(), user.getNickname())));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@LoginUser Long userId, HttpServletResponse response) {
        return userService.logout(userId, response);
    }
}
