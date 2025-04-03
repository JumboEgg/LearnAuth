package ssafy.d210.backend.entity;
//
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import ssafy.d210.backend.dto.request.lecture.SubLectureRequest;

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
    private String subLectureUrl;

    @NotNull
    private int subLectureLength;

    @OneToMany(mappedBy = "subLecture")
    private List<UserLectureTime> userLectureTimeList;


    public static SubLecture from(SubLectureRequest subReq, Lecture savedLecture) {
        SubLecture subLecture = new SubLecture();
        subLecture.setSubLectureTitle(subReq.getSubLectureTitle());
        subLecture.setSubLectureUrl(subReq.getSubLectureUrl());
        subLecture.setSubLectureLength(subReq.getSubLectureLength());
        subLecture.setLecture(savedLecture);
        return subLecture;
    }
}
