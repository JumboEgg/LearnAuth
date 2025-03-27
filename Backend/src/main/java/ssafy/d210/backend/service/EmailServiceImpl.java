package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.email.EmailResponse;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

    private final UserRepository userRepository;
    private final ResponseUtil responseUtil;
    @Override
    public ResponseSuccessDto<EmailResponse> identityEmail(String email) {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new RuntimeException();
        }

        EmailResponse emailResponse = EmailResponse.builder()
                .boolEmail(true)
                .build();

        ResponseSuccessDto<EmailResponse> res = responseUtil.successResponse(emailResponse,HereStatus.SUCCESS_FIND_EMAIL);
        return res;
    }
}
