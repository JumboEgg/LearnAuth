package ssafy.d210.backend.dto.request.lecture;

import lombok.Getter;
import lombok.Setter;
//
@Getter
@Setter
public class SubLectureRequest {
    private String subLectureTitle;
    private String subLectureUrl;
    private int subLectureLength;
}
