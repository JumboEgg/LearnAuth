package ssafy.d210.backend.dto.response.quiz;
//
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class QuizResponse {
    private String question;
    private List<QuizOptionResponse> quizOptions;
}
