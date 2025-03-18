package ssafy.d210.backend.entity;

import jakarta.persistence.*;
import jakarta.persistence.criteria.Fetch;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserLecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_lecture_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;

    @Length(max = 64)
    private String certificate;

    private LocalDate certificateDate;

    @ColumnDefault("0")
    private int recentLectureId;

    @OneToOne
    @JoinColumn(name = "report_id")
    private Report report;

}
