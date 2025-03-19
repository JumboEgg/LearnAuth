package ssafy.d210.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private SubLecture sublecture;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserLecture userlecture;

    private int continueWatching;

    private int endFlag;




}
