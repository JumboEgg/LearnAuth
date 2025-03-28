package ssafy.d210.backend.dto.request.lecture;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
//
@Getter
@Setter
public class MainLectureRequest {
    private List<LectureRequest> mostCompletedLectures = new ArrayList<>();
    private List<LectureRequest> randomLectures = new ArrayList<>();
    private List<LectureRequest> recentLectures = new ArrayList<>();
}
