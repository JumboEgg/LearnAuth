package ssafy.d210.backend.entity;
//
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ssafy.d210.backend.dto.request.quiz.QuizOptionRequest;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class QuizOption {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "quiz_option_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Quiz quiz;

    @NotNull
    private String optionText;

    @NotNull
    private int isCorrect;

    public static QuizOption from(QuizOptionRequest optionReq, Quiz savedQuiz) {
        QuizOption quiz = new QuizOption();
        quiz.setOptionText(optionReq.getQuizOption());
        quiz.setIsCorrect(optionReq.getIsCorrect() ? 1 : 0);
        quiz.setQuiz(savedQuiz);
        return quiz;
    }
}
