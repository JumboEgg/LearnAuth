package ssafy.d210.backend.service;
<<<<<<< Updated upstream

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
=======
//
>>>>>>> Stashed changes
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.user.SignupRequest;
import ssafy.d210.backend.dto.response.user.SignupResponse;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ResponseUtil responseUtil;
    public ResponseSuccessDto<SignupResponse> signup(SignupRequest userSignupRequest) {
        // 닉네임 중복확인
        // 이메일 중복확인
        // 이름 본인인증

        User user = new User();
        user.createUser(userSignupRequest);
        userRepository.save(user);

        SignupResponse userSignupResponse = SignupResponse.builder()
                .nickname(user.getNickname())
                .message("회원가입이 완료되었습니다.")
                .build();
        ResponseSuccessDto<SignupResponse> res = responseUtil.successResponse(userSignupResponse, HereStatus.SUCCESS_SIGNUP);
        return res;
    }
}
