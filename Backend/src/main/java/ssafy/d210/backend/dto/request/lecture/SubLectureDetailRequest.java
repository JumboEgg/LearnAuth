package ssafy.d210.backend.dto.request.lecture;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubLectureDetailRequest {
    private int subLectureId;
    private String subLectureTitle;
    private String lectureUrl;
    private int lectureLength;
    private int continueWatching;
}
