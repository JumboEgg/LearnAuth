package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.quiz.QuizRequest;
import ssafy.d210.backend.dto.request.quiz.QuizResultRequest;
import ssafy.d210.backend.dto.response.quiz.QuizOptionResponse;
import ssafy.d210.backend.dto.response.quiz.QuizResponse;
import ssafy.d210.backend.entity.Quiz;
import ssafy.d210.backend.entity.QuizOption;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.QuizOptionRepository;
import ssafy.d210.backend.repository.QuizRepository;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService{

    private final QuizRepository quizRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final ResponseUtil responseUtil;
    private final UserLectureRepository userLectureRepository;

    @Override
    @Transactional(readOnly = true)
    public ResponseSuccessDto<List<QuizResponse>> getQuizzes(Long lectureId) {

        List<Quiz> allQuizzes = quizRepository.findAllByLectureId(lectureId);

        Collections.shuffle(allQuizzes);
        List<Quiz> randomQuizzes = allQuizzes.stream()
                .limit(3)
                .collect(Collectors.toList());

        List<Long> randomQuizIds = randomQuizzes.stream()
                .map(Quiz::getId)
                .collect(Collectors.toList());

        List<QuizOption> options = quizOptionRepository.findByQuizIdIn(randomQuizIds);

        Map<Long, List<QuizOption>> optionsByQuizId = options.stream()
                .collect(Collectors.groupingBy(option -> option.getQuiz().getId()));

        List<QuizResponse> quizResponses = randomQuizzes.stream()
                .map(quiz -> convertToQuizResponse(quiz, optionsByQuizId.getOrDefault(quiz.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        ResponseSuccessDto<List<QuizResponse>> res = responseUtil.successResponse(quizResponses, HereStatus.SUCCESS_QUIZ);

        return res;
    }

    @Override
    public ResponseSuccessDto<Object> submitQuiz(Long lectureId, QuizResultRequest request) {
        UserLecture userLecture = userLectureRepository.getUserLectureById(lectureId, request.getUserId());

        userLecture.setCertificateDate(LocalDate.now());
        userLectureRepository.save(userLecture);
        ResponseSuccessDto<Object> res = responseUtil.successResponse("ok", HereStatus.SUCCESS_QUIZ_SUBMIT);
        return res;
    }


    private QuizResponse convertToQuizResponse(Quiz quiz, List<QuizOption> options) {
        List<QuizOptionResponse> optionResponses = options.stream()
                .map(this::convertToQuizOptionResponse)
                .collect(Collectors.toList());

        return QuizResponse.builder()
                .question(quiz.getQuestion())
                .quizOptions(optionResponses)
                .build();
    }

    private QuizOptionResponse convertToQuizOptionResponse(QuizOption option) {
        return QuizOptionResponse.builder()
                .quizOption(option.getOptionText())
                .isCorrect(option.getIsCorrect())
                .build();
    }
}
