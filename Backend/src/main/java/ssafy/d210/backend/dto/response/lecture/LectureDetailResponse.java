package ssafy.d210.backend.dto.response.lecture;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
//
@Getter
@Setter
@Builder
public class LectureDetailResponse {
    private long lectureId;
    private Long userLectureId;
    private Long reportId;
    private String title;
    private String categoryName;
    private String goal;
    private String description;
    private int price;
    private String lecturer;
    private String lectureUrl;
    private Long recentLectureId;
    private int studentCount;
    private boolean owned;
    private boolean certificate;
    private String CID;
    private List<SubLectureDetailResponse> subLectures = new ArrayList<>();
}
