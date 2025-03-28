package ssafy.d210.backend.dto.request.quiz;

import lombok.Getter;
import lombok.Setter;
//
@Getter
@Setter
public class QuizResultRequest {
    private boolean completeQuiz;
    private long userId;
}
