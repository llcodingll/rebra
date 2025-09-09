package com.rebra.service;

import com.rebra.entity.User;
import com.rebra.exception.user.UserException;
import com.rebra.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserException::userNotFound);
    }

    @Override
    public ResponseEntity<Void> logout(Long userId, HttpServletResponse response) {

        Cookie clearRefresh = new Cookie("refreshToken", "");
        clearRefresh.setHttpOnly(true);
        clearRefresh.setSecure(true);
        clearRefresh.setPath("/");
        clearRefresh.setMaxAge(0);
        response.addCookie(clearRefresh);

        Cookie clearAccess = new Cookie("accessToken", "");
        clearAccess.setHttpOnly(true);
        clearAccess.setSecure(true);
        clearAccess.setPath("/");
        clearAccess.setMaxAge(0);
        response.addCookie(clearAccess);

        return ResponseEntity.ok().build();
    }
}