package com.rebra.service;

import com.rebra.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {
    User findById(Long userId);

    ResponseEntity<Void> logout(Long userId, HttpServletResponse response);
}
