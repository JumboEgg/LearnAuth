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
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ResponseUtil responseUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final UserLectureRepository userLectureRepository;

    public ResponseSuccessDto<SignupResponse> signup(SignupRequest userSignupRequest) {
        // 닉네임 중복확인
        // 이메일 중복확인
        // 이름 본인인증
        User user = new User();
        user.createUser(userSignupRequest);
        user.setPassword(bCryptPasswordEncoder.encode(userSignupRequest.getPassword()));
        userRepository.save(user);

        SignupResponse userSignupResponse = SignupResponse.builder()
                .nickname(user.getNickname())
                .message("회원가입이 완료되었습니다.")
                .build();
        ResponseSuccessDto<SignupResponse> res = responseUtil.successResponse(userSignupResponse, HereStatus.SUCCESS_SIGNUP);
        return res;
    }
}
