package ssafy.d210.backend.dto.response.lecture;
//
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureResponse {
    private long lectureId;
    private String categoryName;
    private String title;
    private int learningRate;
    private String lecturer;
    private boolean isLecturer;
    private int recentId;

}
