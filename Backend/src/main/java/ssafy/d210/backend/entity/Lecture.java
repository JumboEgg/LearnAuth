package ssafy.d210.backend.entity;
//
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @NotNull
    @Length(max = 32)
    private String title;

    @NotNull
    @Length(max = 64)
    private String goal;

    @NotNull
    @Length(max = 255)
    private String description;

    @NotNull
    private int price;

// 강의 자체 지갑 삭제
//    @Null
//    private String walletKey;

    @OneToMany(mappedBy = "lecture")
    private List<SubLecture> subLectureList;

    @OneToMany(mappedBy = "lecture")
    private List<Quiz> quizList;

    @OneToMany(mappedBy = "lecture")
    private List<PaymentRatio> paymentRatioList;

    @OneToMany(mappedBy = "lecture")
    private  List<UserLecture> userLectureList;

}
