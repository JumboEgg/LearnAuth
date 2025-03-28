package ssafy.d210.backend.dto.response.lecture;
//
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubLectureDetailResponse {
    private long subLectureId;
    private String subLectureTitle;
    private String lectureUrl;
    private int lectureLength;
    private int continueWatching;
}
