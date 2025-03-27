package ssafy.d210.backend.dto.response.lecture;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LectureDetailResponse {
    private long lectureId;
    private long userLectureId;
    private String categoryName;
    private String goal;
    private String description;
    private int price;
    private String name;
    private String lectureUrl;
    private long recentLectureId;
    private int studentCount;
    private List<SubLectureDetailResponse> subLectures = new ArrayList<>();
}
