package ssafy.d210.backend.service;

import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.user.LoginRequest;
import ssafy.d210.backend.dto.request.user.SignupRequest;
import ssafy.d210.backend.dto.response.user.LoginResponse;
import ssafy.d210.backend.dto.response.user.SignupResponse;

public interface UserService {
    public ResponseSuccessDto<SignupResponse> signup(SignupRequest userSignupRequest);


}
