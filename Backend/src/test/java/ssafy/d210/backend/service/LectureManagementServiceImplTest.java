package ssafy.d210.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


// mockito 활용하여 테스트 코드 작성
// mockito : java 오픈소스 테스트 프레임워크
@ExtendWith(MockitoExtension.class)
class LectureManagementServiceImplTest {
    // 필요한 Repository와 유틸을 Mock으로 만들어서 실제 DB 없어도 테스트 가능하게 한다.
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

    @Test
    @DisplayName("강의 등록 성공 테스트")
    void registerLectureSuccess() {
        LectureRegisterRequest request = makeValidRequest();

        //given : 가짜 데이터와 동작 미리 설정
        // 특정 메서드 호출 시 가짜 데이터 반환
        when(categoryRepository.findByCategoryName(any())).thenReturn(Optional.of(new Category()));
        when(lectureRepository.save(any())).thenReturn(new Lecture());
        when(quizRepository.save(any())).thenReturn(new Quiz());
        when(userRepository.findOptionalUserByEmail(anyString())).thenReturn(Optional.of(new User()));
        when(userLectureRepository.save(any())).thenReturn(new UserLecture());

        // 응답 가짜로 만들기
        ResponseSuccessDto<Boolean> responseDto = new ResponseSuccessDto<>();
        responseDto.setData(true);
        responseDto.setStatus(HereStatus.SUCCESS_LECTURE_REGISTERED.name());
        when(responseUtil.successResponse(true, HereStatus.SUCCESS_LECTURE_REGISTERED)).thenReturn(responseDto);

        // when
        ResponseSuccessDto<Boolean> response = lectureManagementService.registerLecture(request);

        // then
        // "assertThat(실제값).비교값"으로 결과 예상한 대로 나오는지 확인
        assertThat(response).isNotNull();
        assertThat(response.getData()).isTrue();
        assertThat(response.getStatus()).isEqualTo(HereStatus.SUCCESS_LECTURE_REGISTERED.name());
    }

    @Test
    void 필수값누락테스트() {
        LectureRegisterRequest request = makeValidRequest();
        request.setTitle(null);
        assertThrows(InvalidLectureDataException.class, () -> lectureManagementService.registerLecture(request));
    }

    @Test
    void 퀴즈2개만있으면예외() {
        LectureRegisterRequest request = makeValidRequest();
        request.setQuizzes(List.of(request.getQuizzes().get(0), request.getQuizzes().get(1))); // 2개만 가져옴
        assertThrows(InvalidQuizDataException.class, () -> lectureManagementService.registerLecture(request));
    }

    @Test
    void 퀴즈옵션_2개만_있으면_예외() {
        LectureRegisterRequest request = makeValidRequest();

        // 카테고리 존재하도록 mock 설정
        when(categoryRepository.findByCategoryName(any())).thenReturn(Optional.of(new Category()));

        // 기존 퀴즈 중 하나 가져오기
        QuizRequest quiz = request.getQuizzes().get(0);

        // 퀴즈 옵션 2개만 생성 (정답은 1개만 포함)
        QuizOptionRequest option1 = new QuizOptionRequest();
        option1.setQuizOption("옵션1");
        option1.setIsCorrect(true);

        QuizOptionRequest option2 = new QuizOptionRequest();
        option2.setQuizOption("옵션2");
        option2.setIsCorrect(false);

        // 해당 퀴즈에 옵션을 2개만 설정
        quiz.setQuizOptions(List.of(option1, option2));

        // 예외 발생 테스트
        assertThrows(InvalidQuizDataException.class, () -> lectureManagementService.registerLecture(request));
    }

}