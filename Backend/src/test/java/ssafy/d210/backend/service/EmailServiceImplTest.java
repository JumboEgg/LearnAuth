package ssafy.d210.backend.service;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
//import ssafy.d210.backend.dto.common.ResponseSuccessDto;
//import ssafy.d210.backend.dto.response.email.EmailResponse;
//import ssafy.d210.backend.enumeration.response.HereStatus;
//import ssafy.d210.backend.exception.service.InValidEmailFormatException;
//import ssafy.d210.backend.repository.UserRepository;
//import ssafy.d210.backend.util.ResponseUtil;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//
@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private ResponseUtil responseUtil;
//
//    @InjectMocks
//    private EmailServiceImpl emailService;
//
//    @Test
//    @DisplayName("이메일이 존재하는 경우")
//    void identityEmail_existingEmail() {
//        // given
//        String email = "test@example.com";
//        when(userRepository.existsByEmail(email)).thenReturn(true);
//
//        EmailResponse mockResponse = EmailResponse.builder().boolEmail(true).build();
//        ResponseSuccessDto<EmailResponse> expected = new ResponseSuccessDto<>();
//        expected.setData(mockResponse);
//        expected.setStatus(HereStatus.SUCCESS_FIND_EMAIL.name());
//
//        when(responseUtil.successResponse(mockResponse, HereStatus.SUCCESS_FIND_EMAIL)).thenReturn(expected);
//
//        // when
//        ResponseSuccessDto<EmailResponse> result = emailService.identityEmail(email);
//
//        // then
//        assertThat(result.getData().isBoolEmail()).isTrue();
//        assertThat(result.getStatus()).isEqualTo(HereStatus.SUCCESS_FIND_EMAIL.name());
//    }
//
//    @Test
//    @DisplayName("이메일이 존재하지 않는 경우")
//    void identityEmail_nonExistingEmail() {
//        // given
//        String email = "test@example.com";
//        when(userRepository.existsByEmail(email)).thenReturn(false);
//
//        EmailResponse mockResponse = EmailResponse.builder().boolEmail(false).build();
//        ResponseSuccessDto<EmailResponse> expected = new ResponseSuccessDto<>();
//        expected.setData(mockResponse);
//        expected.setStatus(HereStatus.SUCCESS_FIND_EMAIL.name());
//
//        when(responseUtil.successResponse(mockResponse, HereStatus.SUCCESS_FIND_EMAIL)).thenReturn(expected);
//
//        // when
//        ResponseSuccessDto<EmailResponse> result = emailService.identityEmail(email);
//
//        // then
//        assertThat(result.getData().isBoolEmail()).isFalse();
//        assertThat(result.getStatus()).isEqualTo(HereStatus.SUCCESS_FIND_EMAIL.name());
//    }
//
//    @Test
//    @DisplayName("이메일 형식이 유효하지 않은 경우")
//    void identityEmail_invalidFormat() {
//        // given
//        String invalidEmail = "invalid-email";
//
//        // when & then
//        assertThrows(InValidEmailFormatException.class, () -> emailService.identityEmail(invalidEmail));
//    }
//
//    @Test
//    @DisplayName("이메일이 null인 경우")
//    void identityEmail_nullEmail() {
//        // when & then
//        assertThrows(InValidEmailFormatException.class, () -> emailService.identityEmail(null));
//    }
//
//    @Test
//    @DisplayName("이메일이 빈 문자열인 경우")
//    void identityEmail_emptyEmail() {
//        // when & then
//        assertThrows(InValidEmailFormatException.class, () -> emailService.identityEmail(""));
//    }
}
