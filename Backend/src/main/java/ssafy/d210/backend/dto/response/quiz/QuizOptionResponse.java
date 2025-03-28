package ssafy.d210.backend.dto.response.quiz;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class QuizOptionResponse {
    private String quizOption;
    private int isCorrect;
}
