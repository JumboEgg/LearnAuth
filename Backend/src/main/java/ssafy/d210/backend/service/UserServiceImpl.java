package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.user.LoginRequest;
import ssafy.d210.backend.dto.request.user.SignupRequest;
import ssafy.d210.backend.dto.response.user.LoginResponse;
import ssafy.d210.backend.dto.response.user.SignupResponse;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.DefaultException;
import ssafy.d210.backend.exception.service.DuplicatedValueException;
import ssafy.d210.backend.exception.service.PasswordIsNotAllowed;
import ssafy.d210.backend.redis.DistributedLock;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.security.entity.Token;
import ssafy.d210.backend.security.jwt.JwtUtil;
import ssafy.d210.backend.security.repository.TokenRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.util.Date;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ResponseUtil responseUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    @DistributedLock(key = "#userSignupRequest.email")
    public ResponseSuccessDto<SignupResponse> signup(SignupRequest userSignupRequest) {

        // 이름 본인인증
        User newUser = new User();
        newUser.createUser(userSignupRequest);

        checkDuplication(newUser.getEmail() ,newUser.getNickname());

        if (userSignupRequest.getPassword().length() < 8 || userSignupRequest.getPassword().length() > 100) {
            log.error("비밀번호가 8자보다 작거나 100자 보다 큽니다.");
            throw new PasswordIsNotAllowed("비밀번호가 8자보다 작거나 100자 보다 큽니다.");
        }

        newUser.setPassword(bCryptPasswordEncoder.encode(userSignupRequest.getPassword()));
        userRepository.save(newUser);

        SignupResponse userSignupResponse = SignupResponse.builder()
                .nickname(newUser.getNickname())
                .message("회원가입이 완료되었습니다.")
                .build();
        ResponseSuccessDto<SignupResponse> res = responseUtil.successResponse(userSignupResponse, HereStatus.SUCCESS_SIGNUP);
        return res;
    }

    @Transactional(readOnly = true)
    protected void checkDuplication(String email, String nickname) {
        boolean emailExists = userRepository.existsByEmail(email);
        if (emailExists) {
            log.error("중복 이메일: {}", email);
            throw new DuplicatedValueException("이미 사용중인 이메일입니다.");
        }

        boolean nicknameExists = userRepository.existsByNickname(nickname);
        if (nicknameExists) {
            log.error("중복 닉네임: {}", nickname);
            throw new DuplicatedValueException("이미 사용중인 닉네임입니다.");
        }
    }
}
