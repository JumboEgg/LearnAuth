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
public class LectureInfoResponse {
    private long lectureId;
    private String title;
    private int price;
    private String lecturer;
    private String lectureUrl;
    private String categoryName;


    public LectureInfoResponse(Long lectureId, String title, int price, String lecturer, String lectureUrl, CategoryName categoryName) {
        this.lectureId = lectureId;
        this.title = title;
        this.price = price;
        this.lecturer = lecturer;
        this.lectureUrl = lectureUrl;
        this.categoryName = (categoryName != null) ? categoryName.name() : null;  // enum.name()을 문자열로 변환
    }
}
