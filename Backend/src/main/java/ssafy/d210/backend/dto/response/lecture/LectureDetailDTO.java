package ssafy.d210.backend.dto.response.lecture;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureDetailDTO {
    private Long lectureId;
    private String title;
    private Integer price;
    private String goal;
    private String description;
    private String lecturer;
    private String lectureUrl;
    private String categoryName;
}
