package ssafy.d210.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Profile;
import org.web3j.abi.datatypes.Bool;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import ssafy.d210.backend.contracts.LectureSystem;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.lecture.LectureRegisterRequest;
import ssafy.d210.backend.dto.request.lecture.SubLectureRequest;
import ssafy.d210.backend.dto.request.payment.RatioRequest;
import ssafy.d210.backend.dto.request.quiz.QuizOptionRequest;
import ssafy.d210.backend.dto.request.quiz.QuizRequest;
import ssafy.d210.backend.entity.*;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.service.InvalidLectureDataException;
import ssafy.d210.backend.exception.service.InvalidQuizDataException;
import ssafy.d210.backend.repository.*;
import ssafy.d210.backend.util.ResponseUtil;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


// mockito 활용하여 테스트 코드 작성
// mockito : java 오픈소스 테스트 프레임워크
@ExtendWith(MockitoExtension.class)
@ConditionalOnExpression("'${spring.profiles.active}' != 'dev'")
@MockitoSettings(strictness = Strictness.LENIENT)
class LectureManagementServiceImplTest {
//    // 필요한 Repository와 유틸을 Mock으로 만들어서 실제 DB 없어도 테스트 가능하게 한다.
    @Mock
    private LectureRepository lectureRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserLectureRepository userLectureRepository;
    @Mock
    private SubLectureRepository subLectureRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizOptionRepository quizOptionRepository;
    @Mock
    private PaymentRatioRepository paymentRatioRepository;
    @Mock
    private UserLectureTimeRepository userLectureTimeRepository;
    @Mock
    private ResponseUtil<Boolean> responseUtil;
    @Mock
    private LectureSystem lectureSystem;

    // 위에서 만든 Mock들을 주입해서 테스트 할 대상인 service 객체 생성
    @InjectMocks
    private LectureManagementServiceImpl lectureManagementService;


    // 유효한 강의 등록 요청 객체 생성하는 헬퍼 메서드 : 모든 필수 필드를 채운 상태로 반환
    // 테스트 할 때 매번 객체 새로 안만들어도 되게 한다.
    private LectureRegisterRequest makeValidRequest() {
        LectureRegisterRequest request = new LectureRegisterRequest();
        request.setTitle("테스트 강의");
        request.setCategoryName("수학");
        request.setGoal("열심히 배우기");
        request.setDescription("나는 졸려요");
        request.setPrice(1000);

        // 소강의 1개 추가
        SubLectureRequest subLecture = new SubLectureRequest();
        subLecture.setSubLectureTitle("1강");
        subLecture.setSubLectureUrl("https://www.youtube.com/watch?v=XCadBUnbd2I");
        subLecture.setSubLectureLength(100);
        request.setSubLectures(List.of(subLecture));

        // 퀴즈 옵션 3개
        QuizOptionRequest option1 = new QuizOptionRequest();
        option1.setQuizOption("옵션1");
        option1.setIsCorrect(true);
        QuizOptionRequest option2 = new QuizOptionRequest();
        option2.setQuizOption("옵션2");
        option2.setIsCorrect(false);
        QuizOptionRequest option3 = new QuizOptionRequest();
        option3.setQuizOption("옵션3");
        option3.setIsCorrect(false);

        QuizRequest quiz = new QuizRequest();
        quiz.setQuestion("질문1");
        quiz.setQuizOptions(List.of(option1, option2, option3));

        request.setQuizzes(List.of(quiz, quiz, quiz));

        // 강의자 1명 등록
        RatioRequest ratio = new RatioRequest();
        ratio.setEmail("1@gmail.com");
        ratio.setRatio(100);
        ratio.setLecturer(true);
        request.setRatios(List.of(ratio));

        return request;
    }

    private ResponseSuccessDto<Boolean> successResponse() {
        ResponseSuccessDto<Boolean> successDto = new ResponseSuccessDto<>();
        successDto.setData(true);
        successDto.setStatus(HereStatus.SUCCESS_LECTURE_REGISTERED.name());
        return successDto;
    }

    private ResponseSuccessDto<Boolean> failResponse() {
        ResponseSuccessDto<Boolean> failDto = new ResponseSuccessDto<>();
        failDto.setData(false);
        failDto.setStatus(HereStatus.FAIL_LECTURE_REGISTERED.name());
        return failDto;
    }

    private void prepareBasicMocks(LectureRegisterRequest request) throws Exception {
        when(categoryRepository.findByCategoryName(any())).thenReturn(Optional.of(new Category()));
        Lecture savedLecture = new Lecture();
        savedLecture.setId(100L);
        when(lectureRepository.save(any())).thenReturn(savedLecture);
        when(quizRepository.save(any())).thenReturn(new Quiz());

        User user = new User();
        user.setId(1L);
        when(userRepository.findOptionalUserByEmail(anyString())).thenReturn(Optional.of(user));
        when(userLectureRepository.save(any())).thenReturn(new UserLecture());

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus("0x1");

        RemoteFunctionCall<TransactionReceipt> mockCall = mock(RemoteFunctionCall.class);
        when(mockCall.send()).thenReturn(receipt);
        when(lectureSystem.createLecture(any(),any(),anyList())).thenReturn(mockCall);

       when(responseUtil.successResponse(eq(true), eq(HereStatus.SUCCESS_LECTURE_REGISTERED))).thenReturn(successResponse());
       when(responseUtil.successResponse(eq(false), eq(HereStatus.FAIL_LECTURE_REGISTERED))).thenReturn(failResponse());
    }

    @Test
    void 강의등록성공() throws Exception {
        LectureRegisterRequest request = makeValidRequest();
        prepareBasicMocks(request);

        ResponseSuccessDto<Boolean> res = lectureManagementService.registerLecture(request);

        assertThat(res.getData()).isTrue();
        assertThat(res.getStatus()).isEqualTo(HereStatus.SUCCESS_LECTURE_REGISTERED.name());
    }



//
//
//
//
//    @Test
//    void 강의제목누락실패() throws Exception {
//        LectureRegisterRequest request = makeValidRequest();
//        request.setTitle(null);
//        prepareBasicMocks(request);
//
//        ResponseSuccessDto<Boolean> response = lectureManagementService.registerLecture(request);
//
//        assertThat(response.getData()).isFalse();
//        assertThat(response.getStatus()).isEqualTo(HereStatus.FAIL_LECTURE_REGISTERED.name());
//    }
//
//    @Test
//    void 퀴즈3개미만실패() throws Exception {
//        LectureRegisterRequest request = makeValidRequest();
//        request.setQuizzes(List.of(request.getQuizzes().get(0)));
//        prepareBasicMocks(request);
//
//        ResponseSuccessDto<Boolean> response = lectureManagementService.registerLecture(request);
//
//        assertThat(response.getData()).isFalse();
//        assertThat(response.getStatus()).isEqualTo((HereStatus.FAIL_LECTURE_REGISTERED.name()));
//    }
//
//    @Test
//    void ratio없음실패() throws Exception {
//        LectureRegisterRequest request = makeValidRequest();
//        request.setRatios(null);
//        prepareBasicMocks(request);
//
//        ResponseSuccessDto<Boolean> res = lectureManagementService.registerLecture(request);
//
//        assertThat(res.getData()).isFalse();
//        assertThat(res.getStatus()).isEqualTo(HereStatus.FAIL_LECTURE_REGISTERED.name());
//    }
//
//    @Test
//    void 중복이메일실패() throws Exception {
//        LectureRegisterRequest request = makeValidRequest();
//        RatioRequest r2 = new RatioRequest();
//        r2.setEmail("1@gmail.com");
//        r2.setRatio(0);
//        r2.setLecturer(false);
//        request.setRatios(List.of(request.getRatios().get(0), r2));
//        prepareBasicMocks(request);
//
//        ResponseSuccessDto<Boolean> res = lectureManagementService.registerLecture(request);
//
//        assertThat(res.getData()).isFalse();
//        assertThat(res.getStatus()).isEqualTo(HereStatus.FAIL_LECTURE_REGISTERED.name());
//    }


}