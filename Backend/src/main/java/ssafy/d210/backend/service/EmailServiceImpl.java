package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.email.EmailResponse;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.service.InValidEmailFormatException;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService{

    private final UserRepository userRepository;
    private final ResponseUtil responseUtil;
    @Override
    public ResponseSuccessDto<EmailResponse> identityEmail(String email) {
        boolean isValidFormat = validateEmailFormat(email);

        boolean isEmail = false;
        if (!isValidFormat) {
            log.error("이메일 형식에 맞지 않습니다. email : {}", email);
            throw new InValidEmailFormatException("이메일 형식에 맞지 않습니다.");
        }
        isEmail = userRepository.existsByEmail(email);
        EmailResponse emailResponse = EmailResponse.builder()
                .boolEmail(isEmail)
                .build();

        ResponseSuccessDto<EmailResponse> res = responseUtil.successResponse(emailResponse, HereStatus.SUCCESS_FIND_EMAIL);
        return res;
    }

    private boolean validateEmailFormat(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        // 이메일 정규표현식 패턴
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}
