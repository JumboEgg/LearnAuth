package ssafy.d210.backend.dto.response.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ssafy.d210.backend.enumeration.CategoryName;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureInfoListResponse {
    private long lectureId;
    private String title;
    private int price;
    private String lecturer;
    private String lectureUrl;
    private String categoryName;
}
