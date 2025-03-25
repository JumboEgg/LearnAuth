package ssafy.d210.backend.dto.response.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizOptionResponse {
    private String quizOption;
    private Boolean isCorrect;
}
