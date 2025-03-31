package ssafy.d210.backend.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.user.SignupRequest;
import ssafy.d210.backend.dto.response.user.SignupResponse;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.service.DuplicatedValueException;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResponseUtil responseUtil;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private SignupRequest signupRequest;
    private User user;
    private SignupResponse signupResponse;
    private ResponseSuccessDto<SignupResponse> responseResponseSuccessDto;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@gmail.com");
        signupRequest.setName("김싸피");
        signupRequest.setNickname("test");
        signupRequest.setPassword("12345678");
        signupRequest.setWallet("asdkhjfaskhjfae");
        signupRequest.setUserKey("12asknhjfsdkljfsdk");

        user = new User();
        user.createUser(signupRequest);

        signupResponse = SignupResponse.builder()
                .nickname(user.getNickname())
                .message("회원가입이 완료되었습니다.")
                .build();

        responseResponseSuccessDto = new ResponseSuccessDto<>();
        responseResponseSuccessDto.setData(signupResponse);
        responseResponseSuccessDto.setCode(200);
        responseResponseSuccessDto.setStatus(HereStatus.SUCCESS_LOGIN.name());
    }
    @Test
    @DisplayName("회원가입 성공 테스트")
    void 회원가입_성공() {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(userRepository.existsByNickname(anyString())).willReturn(false);
        given(bCryptPasswordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(responseUtil.successResponse(any(SignupResponse.class), any(HereStatus.class))).willReturn(responseResponseSuccessDto);

        // when
        ResponseSuccessDto<SignupResponse> res = userService.signup(signupRequest);

        // then
        Assertions.assertThat(res).isNotNull();
        Assertions.assertThat(res.getData().getNickname()).isEqualTo("test");
        Assertions.assertThat(res.getData().getMessage()).isEqualTo("회원가입이 완료되었습니다.");

        verify(userRepository).existsByEmail(signupRequest.getEmail());
        verify(userRepository).existsByNickname(signupRequest.getNickname());
        verify(bCryptPasswordEncoder).encode(signupRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }
    @Test
    @DisplayName("이메일 중복 시 예외 발생 테스트")
    void 이메일_중복_체크() {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when
        DuplicatedValueException exception = org.junit.jupiter.api.Assertions.assertThrows(DuplicatedValueException.class,
                () -> userService.signup(signupRequest));

        // then
        Assertions.assertThat(exception.getMessage()).isEqualTo("이미 사용중인 이메일입니다.");
        verify(userRepository).existsByEmail(signupRequest.getEmail());
    }
}