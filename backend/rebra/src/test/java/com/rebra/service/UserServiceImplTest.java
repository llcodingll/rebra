package com.rebra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.rebra.dto.request.SignupRequest;
import com.rebra.entity.SurveyResult;
import com.rebra.entity.User;
import com.rebra.exception.signup.SignupException;
import com.rebra.exception.user.UserException;
import com.rebra.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String USER_SUB_NEW = "new-user-sub";
    private static final String NICKNAME_NEW_USER = "신규사용자";
    private static final String NICKNAME_EXISTING_USER = "기존사용자";
    private static final String DUPLICATE_NICKNAME = "중복닉네임";

    @Mock
    private UserRepository userRepository;

    @Mock
    private SignupService signupService;

    @InjectMocks
    private UserServiceImpl userService;

    private User createUser(Long id, String sub, String nickname) {
        SurveyResult surveyResult = SurveyResult.builder()
                .age(30)
                .mainIncomeSource("급여소득")
                .investmentPurpose(25)
                .investmentExperience(26)
                .riskTolerance(25)
                .build();

        User user = User.builder()
                .sub(sub)
                .nickname(nickname)
                .surveyResult(surveyResult)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("신규 사용자 생성 성공")
    void createUser_Success() {
        // given
        SignupRequest signupRequest = new SignupRequest(NICKNAME_NEW_USER, 30, "급여소득", 25, 26, 25);
        User savedUser = createUser(1L, USER_SUB_NEW, NICKNAME_NEW_USER);
        
        given(signupService.isNicknameAvailable(NICKNAME_NEW_USER)).willReturn(true);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        Long result = userService.createUser(USER_SUB_NEW, signupRequest);

        // then
        assertThat(result).isEqualTo(1L);
        verify(signupService).isNicknameAvailable(NICKNAME_NEW_USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 닉네임으로 사용자 생성 실패")
    void createUser_DuplicateNickname_ThrowsException() {
        // given
        SignupRequest signupRequest = new SignupRequest(DUPLICATE_NICKNAME, 30, "급여소득", 25, 26, 25);
        
        given(signupService.isNicknameAvailable(DUPLICATE_NICKNAME)).willReturn(false);

        // when & then
        assertThrows(SignupException.class, 
                () -> userService.createUser(USER_SUB_NEW, signupRequest));
        
        verify(signupService).isNicknameAvailable(DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("사용자 닉네임 조회 성공")
    void getUserNickname_Success() {
        // given
        User user = createUser(1L, USER_SUB_NEW, NICKNAME_EXISTING_USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        String result = userService.getUserNickname(1L);

        // then
        assertThat(result).isEqualTo(NICKNAME_EXISTING_USER);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 닉네임 조회 실패")
    void getUserNickname_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserException.class, 
                () -> userService.getUserNickname(999L));
        
        verify(userRepository).findById(999L);
    }
}