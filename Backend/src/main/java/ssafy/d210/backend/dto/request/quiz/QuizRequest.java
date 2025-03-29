package ssafy.d210.backend.dto.request.quiz;

import lombok.Getter;
import lombok.Setter;
import ssafy.d210.backend.entity.QuizOption;

import java.util.ArrayList;
import java.util.List;
//
@Getter
@Setter
public class QuizRequest {
    private String question;
    private List<QuizOptionRequest> quizOptions = new ArrayList<>();

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<QuizOptionRequest> getQuizOptions() { return quizOptions; }
    public void setQuizOptions(List<QuizOptionRequest> quizOptions) { this.quizOptions = quizOptions; }
}
