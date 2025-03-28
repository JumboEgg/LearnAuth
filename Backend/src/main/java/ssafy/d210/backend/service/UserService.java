package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.user.SignupRequest;
import ssafy.d210.backend.dto.response.user.SignupResponse;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;


public interface UserService {
    public ResponseSuccessDto<SignupResponse> signup(SignupRequest userSignupRequest);
}
