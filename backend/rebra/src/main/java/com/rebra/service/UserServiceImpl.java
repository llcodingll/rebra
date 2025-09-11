package com.rebra.service;

import com.rebra.dto.request.SignupRequest;
import com.rebra.dto.response.UserProfileResponse;
import com.rebra.entity.SurveyResult;
import com.rebra.entity.User;
import com.rebra.exception.signup.SignupException;
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
    private final SignupService signupService;

    @Override
    public UserProfileResponse findById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException::userNotFound);
        return new UserProfileResponse(user.getId(), user.getNickname());
    }

    @Override
    public Long createUser(String socialId, SignupRequest signupRequest) {
        // 닉네임 중복 확인
        if (!signupService.isNicknameAvailable(signupRequest.getNickname())) {
            throw SignupException.nicknameAlreadyExists();
        }
        
        SurveyResult surveyResult = SurveyResult.builder()
                .age(signupRequest.getAge())
                .mainIncomeSource(signupRequest.getMainIncomeSource())
                .investmentPurpose(signupRequest.getInvestmentPurpose())
                .investmentExperience(signupRequest.getInvestmentExperience())
                .riskTolerance(signupRequest.getRiskTolerance())
                .build();
                
        User user = User.builder()
                .sub(socialId)
                .nickname(signupRequest.getNickname())
                .surveyResult(surveyResult)
                .build();
        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    @Override
    public String getUserNickname(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException::userNotFound);
        return user.getNickname();
    }
}