package ssafy.d210.backend.dto.request.quiz;

import lombok.Getter;
import lombok.Setter;
//
@Getter
@Setter
public class QuizOptionRequest {
    private String quizOption;
    private Boolean isCorrect;

    public String getQuizOption() { return quizOption; }
    public void setQuizOption(String quizOption) { this.quizOption = quizOption; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
}
