//package ssafy.d210.backend.service;
//
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
//import org.springframework.context.annotation.Profile;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import ssafy.d210.backend.dto.common.ResponseSuccessDto;
//import ssafy.d210.backend.dto.request.user.SignupRequest;
//import ssafy.d210.backend.dto.response.user.SignupResponse;
//import ssafy.d210.backend.entity.User;
//import ssafy.d210.backend.enumeration.response.HereStatus;
//import ssafy.d210.backend.exception.service.DuplicatedValueException;
//import ssafy.d210.backend.repository.UserRepository;
//import ssafy.d210.backend.util.ResponseUtil;
//
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@ConditionalOnExpression("'${spring.profiles.active}' != 'dev'")
//class UserServiceImplTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private ResponseUtil responseUtil;
//
//    @Mock
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @InjectMocks
//    private UserServiceImpl userService;
//
//    private SignupRequest signupRequest;
//    private User user;
//    private SignupResponse signupResponse;
//    private ResponseSuccessDto<SignupResponse> responseResponseSuccessDto;
//
//    @BeforeEach
//    void setup() {
//        signupRequest = new SignupRequest(
//                "test@gmail.com"
//                ,"김싸피"
//                ,"test",
//                "12345678",
//                "asdkhjfaskhjfae"
//        );
//        user = new User();
//        user.createUser(signupRequest);
//
//        signupResponse = SignupResponse.builder()
//                .nickname(user.getNickname())
//                .message("회원가입이 완료되었습니다.")
//                .build();
//
//        responseResponseSuccessDto = new ResponseSuccessDto<>();
//        responseResponseSuccessDto.setData(signupResponse);
//        responseResponseSuccessDto.setCode(200);
//        responseResponseSuccessDto.setStatus(HereStatus.SUCCESS_LOGIN.name());
//    }
////    @Test
////    @DisplayName("회원가입 성공 테스트")
////    void 회원가입_성공() {
////        // given
////        given(userRepository.existsByEmail(anyString())).willReturn(false);
////        given(userRepository.existsByNickname(anyString())).willReturn(false);
////        given(bCryptPasswordEncoder.encode(anyString())).willReturn("encodedPassword");
////        given(responseUtil.successResponse(any(SignupResponse.class), any(HereStatus.class))).willReturn(responseResponseSuccessDto);
////
////        // when
////        ResponseSuccessDto<SignupResponse> res = userService.signup(signupRequest);
////
////        // then
////        Assertions.assertThat(res).isNotNull();
////        Assertions.assertThat(res.getData().getNickname()).isEqualTo("test");
////        Assertions.assertThat(res.getData().getMessage()).isEqualTo("회원가입이 완료되었습니다.");
////
////        verify(userRepository).existsByEmail(signupRequest.getEmail());
////        verify(userRepository).existsByNickname(signupRequest.getNickname());
////        verify(bCryptPasswordEncoder).encode(signupRequest.getPassword());
////        verify(userRepository).save(any(User.class));
////    }
//    @Test
//    @DisplayName("이메일 중복 시 예외 발생 테스트")
//    void 이메일_중복_체크() {
//        // given
//        given(userRepository.existsByEmail(anyString())).willReturn(true);
//
//        // when
//        DuplicatedValueException exception = org.junit.jupiter.api.Assertions.assertThrows(DuplicatedValueException.class,
//                () -> userService.signup(signupRequest));
//
//        // then
//        Assertions.assertThat(exception.getMessage()).isEqualTo("이미 사용중인 이메일입니다.");
//        verify(userRepository).existsByEmail(signupRequest.getEmail());
//    }
//    @Test
//    @DisplayName("동시에 동일한 이메일로 회원가입 요청 시 분산 락 테스트")
//    void 동시성_테스트() throws InterruptedException {
//        // given
//        int threadCount = 5;
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        SignupRequest request = new SignupRequest(
//                "concurrent@example.com",
//                "password12345",
//                "concurrent",
//                "asdasdasd",
//                "동시성테스트"
//        );
//
//        // 더 엄격한 동시성 제어를 위한 변수
//        final Object lock = new Object();
//        AtomicBoolean firstEmailCheck = new AtomicBoolean(true);
//
//        // Mock 설정: 동기화된 방식으로 첫 번째 호출만 false 반환
//        when(userRepository.existsByEmail(anyString())).thenAnswer(invocation -> {
//            synchronized (lock) {
//                if (firstEmailCheck.get()) {
//                    firstEmailCheck.set(false);
//                    return false;  // 첫 번째 호출은 중복 아님
//                } else {
//                    return true;   // 두 번째 이후 호출은 중복
//                }
//            }
//        });
//
//        // 닉네임 중복 체크도 통과하도록 설정
//        when(userRepository.existsByNickname(anyString())).thenReturn(false);
//
//        // 비밀번호 인코딩
//        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
//
//        // 저장 후 응답 설정
//        User savedUser = new User();
//        savedUser.setNickname("concurrent");
//        when(userRepository.save(any(User.class))).thenReturn(savedUser);
//
//        // 응답 객체 설정
//        SignupResponse signupResponse = SignupResponse.builder()
//                .nickname("concurrent")
//                .message("회원가입이 완료되었습니다.")
//                .build();
//        ResponseSuccessDto<SignupResponse> responseDto = new ResponseSuccessDto<>();
//        responseDto.setData(signupResponse);
//        responseDto.setStatus(HereStatus.SUCCESS_SIGNUP.name());
//
//        when(responseUtil.successResponse(any(SignupResponse.class), any(HereStatus.class)))
//                .thenReturn(responseDto);
//
//        AtomicInteger successCount = new AtomicInteger();
//        AtomicInteger exceptionCount = new AtomicInteger();
//
//        // when
//        for (int i = 0; i < threadCount; i++) {
//            final int threadIndex = i;
//            executorService.submit(() -> {
//                try {
//                    System.out.println("스레드 " + threadIndex + " 시도 중");
//                    userService.signup(request);
//                    System.out.println("스레드 " + threadIndex + " 성공!");
//                    successCount.incrementAndGet();
//                } catch (Exception e) {
//                    System.out.println("스레드 " + threadIndex + " 실패: " + e.getMessage());
//                    exceptionCount.incrementAndGet();
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await();
//        executorService.shutdown();
//
//        // then
//        assertEquals(1, successCount.get());  // 하나만 성공
//        assertEquals(4, exceptionCount.get());  // 나머지는 예외
//
//        // 회원가입 메서드가 한 번만 호출되었는지 확인
//        verify(userRepository, times(5)).existsByEmail(anyString()); // 모든 스레드가 이메일 체크
//        verify(userRepository, times(1)).existsByNickname(anyString()); // 첫 번째 스레드만 닉네임 체크
//        verify(userRepository, times(1)).save(any(User.class)); // 첫 번째 스레드만 저장
//    }
//}