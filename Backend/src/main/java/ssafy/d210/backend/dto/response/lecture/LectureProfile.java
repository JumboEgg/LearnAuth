package ssafy.d210.backend.dto.response.lecture;
//
import lombok.*;

@Getter
@Setter
public class LectureProfile {
    private long lectureId;
    private String categoryName;
    private String title;
    private String lecturer;
    private Boolean isLecturer;
    private Long recentId;

    public LectureProfile(long lectureId, String categoryName, String title, String lecturer, Integer isLecturer, Long recentId) {
        this.lectureId = lectureId;
        this.categoryName = categoryName;
        this.title = title;
        this.lecturer = lecturer;
        this.isLecturer = isLecturer != null && isLecturer == 1;
        this.recentId = recentId;
    }
}
