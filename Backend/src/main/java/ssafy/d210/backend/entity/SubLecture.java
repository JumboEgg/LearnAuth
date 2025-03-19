package ssafy.d210.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SubLecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_lecture_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lecture lecture;

    @NotNull
    @Length(max=32)
    private String subLectureTitle;

    @NotNull
    @Length(max=100)
    private String lectureUrl;

    @NotNull
    private int lectureLength;

    @NotNull
    private int continueWatching;

    private int endFlag;

    @OneToMany(mappedBy = "sublecture")
    private List<UserLectureTime> userLectureTimeList;
}
