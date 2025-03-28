package ssafy.d210.backend.dto.request.lecture;

import lombok.Getter;
import lombok.Setter;

//
@Getter
@Setter
public class LectureRequest {
    private int lectureId;
    private String title;
    private int price;
    private String name;
    private String lectureUrl;
    private String categoryName;

}
