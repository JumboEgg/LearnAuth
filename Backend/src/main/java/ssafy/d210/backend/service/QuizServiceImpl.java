package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.quiz.QuizRequest;
import ssafy.d210.backend.dto.response.quiz.QuizResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService{
    @Override
    public List<QuizResponse> getQuizzes(Long lectureId) {
        return null;
    }

    @Override
    public QuizResponse submitQuiz(Long lectureId, QuizRequest request) {
        return null;
    }
}
