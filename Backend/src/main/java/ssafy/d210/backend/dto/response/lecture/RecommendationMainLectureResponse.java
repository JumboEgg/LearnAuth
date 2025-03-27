package ssafy.d210.backend.dto.response.lecture;

import lombok.Getter;
import lombok.Setter;
import ssafy.d210.backend.dto.request.lecture.LectureRequest;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RecommendationMainLectureResponse {
    private List<RecommendationLectureResponse> mostCompletedLectures = new ArrayList<>();
    private List<RecommendationLectureResponse> randomLectures = new ArrayList<>();
    private List<RecommendationLectureResponse> recentLectures = new ArrayList<>();
}
