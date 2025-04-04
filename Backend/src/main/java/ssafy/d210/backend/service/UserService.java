package ssafy.d210.backend.service;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.user.SignupRequest;
import ssafy.d210.backend.dto.response.user.SignupResponse;
public interface UserService {
    ResponseSuccessDto<SignupResponse> signup(SignupRequest userSignupRequest);
}
