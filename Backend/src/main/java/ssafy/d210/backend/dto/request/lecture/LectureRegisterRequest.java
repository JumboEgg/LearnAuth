package ssafy.d210.backend.dto.request.lecture;
//
import lombok.Getter;
import lombok.Setter;
import ssafy.d210.backend.dto.request.payment.RatioRequest;
import ssafy.d210.backend.dto.request.quiz.QuizRequest;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LectureRegisterRequest {
    private String title;
    private String categoryName;
    private String goal;
    private String description;
    private int price;
    private String walletKey;
    private List<RatioRequest> ratios = new ArrayList<>();
    private List<SubLectureRequest> subLectures = new ArrayList<>();
    private List<QuizRequest> quizzes = new ArrayList<>();

}
