package ssafy.d210.backend.service;

import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.quiz.QuizRequest;
import ssafy.d210.backend.dto.response.quiz.QuizResponse;

import java.util.List;

@Service
public interface QuizService {

    // 강의별 퀴즈 목록 조회
    public List<QuizResponse> getQuizzes(Long lectureId);

    // 퀴즈 제출
    public QuizResponse submitQuiz(Long lectureId, QuizRequest request);

}
