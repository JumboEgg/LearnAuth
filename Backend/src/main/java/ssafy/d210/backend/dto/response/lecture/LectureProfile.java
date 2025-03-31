package ssafy.d210.backend.dto.response.lecture;
//
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureProfile {
    private long lectureId;
    private String categoryName;
    private String title;
    private String lecturer;
    private Boolean isLecturer;
    private Long recentId;
}
