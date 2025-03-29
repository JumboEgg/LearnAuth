package ssafy.d210.backend.dto.response.lecture;
//
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// TODO : learningRate 추가하기. 별도로 계산해야 정상 작동
public class LectureResponse {
    private long lectureId;
    private String categoryName;
    private String title;
    private String lecturer;
    private Boolean isLecturer;
    private Long recentId;
}
