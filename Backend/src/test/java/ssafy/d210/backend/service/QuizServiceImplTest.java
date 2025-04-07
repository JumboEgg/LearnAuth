package ssafy.d210.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.quiz.QuizResultRequest;
import ssafy.d210.backend.dto.request.user.SignupRequest;
import ssafy.d210.backend.entity.Category;
import ssafy.d210.backend.entity.Lecture;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.enumeration.CategoryName;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.LectureRepository;
import ssafy.d210.backend.repository.ReportRepository;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {
//    @Mock
//    private UserLectureRepository userLectureRepository;
//    @Mock
//    private ResponseUtil responseUtil;
//    @InjectMocks
//    private QuizServiceImpl quizService;
//
//    private User user;
//    private Lecture lecture;
//    private UserLecture userLecture;
//    private SignupRequest signupRequest;
//    private QuizResultRequest quizResultRequest;
//
//    private ResponseSuccessDto<Boolean> responseResponseSuccessDto;
//
//    @BeforeEach
//    void setup() {
//
//        Long lectureId = 1L;
//        Long userId = 100L;
//        signupRequest = new SignupRequest();
//        signupRequest.setEmail("test@gmail.com");
//        signupRequest.setName("김싸피");
//        signupRequest.setNickname("test");
//        signupRequest.setWallet("asdkhjfaskhjfae");
//        signupRequest.setPassword("12345678");
//        user = new User();
//        user.createUser(signupRequest);
//        user.setId(userId);
//
//        lecture = new Lecture();
//        lecture.setCategory(new Category());
//        lecture.setTitle("stringstring");
//        lecture.setGoal("stringstring");
//        lecture.setDescription("stringstring");
//        lecture.setPrice(1000);
//        lecture.setId(lectureId);
//
//        userLecture = new UserLecture();
//        userLecture.setLecture(lecture);
//        userLecture.setUser(user);
//        userLecture.setCertificate(0);
//        userLecture.setCertificateDate(null);
//
//        quizResultRequest = new QuizResultRequest();
//        quizResultRequest.setCompleteQuiz(true);
//
//        responseResponseSuccessDto = new ResponseSuccessDto<>();
//        responseResponseSuccessDto.setData(true);
//        responseResponseSuccessDto.setCode(200);
//        responseResponseSuccessDto.setStatus(HereStatus.SUCCESS_QUIZ_SUBMIT.name());
//
//
//    }
//
//    @Test
//    @DisplayName("퀴즈 제출 테스트")
//    void 퀴즈_제출() {
//        // given
//        Long lectureId = lecture.getId();
//        Long userId = user.getId();
//
//        // 레포지토리 메서드 모킹
//        when(userLectureRepository.getUserLectureById(lectureId, userId))
//                .thenReturn(userLecture);
//
//        when(responseUtil.successResponse(true, HereStatus.SUCCESS_QUIZ_SUBMIT))
//                .thenReturn(responseResponseSuccessDto);
//
//
//        // when
//        ResponseSuccessDto<Boolean> res = quizService.submitQuiz(lectureId, userId, quizResultRequest);
//
//        // then
//        assertNotNull(res);
//        assertTrue(true);
//
//        verify(userLectureRepository).save(userLecture);
//
//    }


    // given


    // when

    // then


}