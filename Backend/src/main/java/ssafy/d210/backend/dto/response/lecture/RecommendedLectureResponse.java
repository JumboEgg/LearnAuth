package ssafy.d210.backend.dto.response.lecture;
//
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RecommendedLectureResponse {
    private List<LectureInfoListResponse> mostCompletedLectures = new ArrayList<>();
    private List<LectureInfoListResponse> randomLectures = new ArrayList<>();
    private List<LectureInfoListResponse> recentLectures = new ArrayList<>();
}
