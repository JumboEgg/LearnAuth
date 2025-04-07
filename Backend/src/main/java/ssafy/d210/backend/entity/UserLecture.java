package ssafy.d210.backend.entity;
//
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

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

    private Integer certificate;

    private LocalDate certificateDate;

    @ColumnDefault("0")
    private Long recentLectureId;

    @Column(columnDefinition = "TEXT")
    private String qrCode;

    @OneToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @OneToMany(mappedBy = "userLecture")
    private List<UserLectureTime> userLectureTimeList;

    public void createUserLecture(User user, Lecture lecture, Long recentLectureId) {
        this.user = user;
        this.lecture = lecture;
        this.recentLectureId = recentLectureId;
    }
}