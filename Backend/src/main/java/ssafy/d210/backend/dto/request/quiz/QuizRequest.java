package ssafy.d210.backend.dto.request.quiz;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuizRequest {
    private String question;
    private List<QuizOptionRequest> quizOptions = new ArrayList<>();
}
