package ssafy.d210.backend.dto.response.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureInfoResponse {
    private long lectureId;
    private String title;
    private int price;
    private String lecturer;
    private String lectureUrl;
    private String categoryName;
}
