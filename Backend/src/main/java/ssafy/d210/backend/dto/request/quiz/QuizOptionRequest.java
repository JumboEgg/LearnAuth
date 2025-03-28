package ssafy.d210.backend.dto.request.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizOptionRequest {
    private String quizOption;
    private Boolean isCorrect;
}
