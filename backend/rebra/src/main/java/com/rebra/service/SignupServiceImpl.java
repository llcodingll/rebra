package com.rebra.service;

import com.rebra.dto.request.SignupRequest;
import com.rebra.dto.response.SignupResponse;
import com.rebra.entity.User;
import com.rebra.exception.user.UserException;
import com.rebra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupServiceImpl implements SignupService {

    private final UserRepository userRepository;

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Override
    @Transactional
    public SignupResponse completeSignup(Long userId, SignupRequest signupRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException::userNotFound);

        if (user.getNickname() != null) {
            throw new IllegalStateException("이미 회원가입이 완료된 사용자입니다.");
        }

        if (!isNicknameAvailable(signupRequest.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User updatedUser = User.builder()
                .id(user.getId())
                .sub(user.getSub())
                .password(user.getPassword())
                .phoneNumber(user.getPhoneNumber())
                .nickname(signupRequest.getNickname())
                .build();

        User savedUser = userRepository.save(updatedUser);
        
        return new SignupResponse(savedUser.getId(), savedUser.getNickname(), "회원가입이 완료되었습니다.");
    }

}