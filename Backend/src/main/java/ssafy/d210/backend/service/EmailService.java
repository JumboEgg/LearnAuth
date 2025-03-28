package ssafy.d210.backend.service;
//
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.email.EmailResponse;

public interface EmailService {

    public ResponseSuccessDto<EmailResponse> identityEmail(String email);
}
