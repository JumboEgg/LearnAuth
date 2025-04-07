package ssafy.d210.backend.dto.request.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizOptionRequest {
    private String quizOption;
    private Boolean isCorrect;

}
