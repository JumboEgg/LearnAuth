package ssafy.d210.backend.dto.request.lecture;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
//
@Getter
@Setter
public class LectureDetailRequest {
    private long lectureId;
    private long userLectureId;
    private String title;
    private String categoryName;
    private String goal;
    private String description;
    private int price;
    private String name;
    private String lectureUrl;
    private int recentLectureId;
    private int studentCount;
    private List<SubLectureDetailRequest> subLectures = new ArrayList<>();
}
