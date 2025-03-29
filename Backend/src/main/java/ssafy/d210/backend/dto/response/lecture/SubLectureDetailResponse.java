package ssafy.d210.backend.dto.response.lecture;
//
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubLectureDetailResponse {
    private long subLectureId;
    private String subLectureTitle;
    private String lectureUrl;
    private int lectureLength;
    private String continueWatching;
}
