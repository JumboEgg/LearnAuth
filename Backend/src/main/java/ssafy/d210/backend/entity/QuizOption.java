package ssafy.d210.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class QuizOption {
    @GeneratedValue
    @Id
    @Column(name = "quiz_option")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Quiz quiz;

    @NotNull
    private String option;

    @NotNull
    private int isCorrect;
}
