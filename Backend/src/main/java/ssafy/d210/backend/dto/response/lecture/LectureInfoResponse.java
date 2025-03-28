package ssafy.d210.backend.dto.response.lecture;

import lombok.Getter;
import lombok.Setter;
//
@Getter
@Setter
public class LectureInfoResponse {
    private long lectureId;
    private String title;
    private int price;
    private String lecturer;
    private String lectureUrl;
    private String categoryName;
}
