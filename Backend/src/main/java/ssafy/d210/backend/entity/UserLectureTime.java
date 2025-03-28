package ssafy.d210.backend.entity;
//
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserLectureTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_lecture_time_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private SubLecture subLecture;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserLecture userLecture;

    private String continueWatching;

    private int endFlag = 0;

    public void createUserLectureTime(UserLecture userLecture, SubLecture subLecture) {
        this.userLecture = userLecture;
        this.subLecture = subLecture;
    }
}
