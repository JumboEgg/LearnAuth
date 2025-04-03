package ssafy.d210.backend.dto.request.lecture;

import lombok.Getter;
import lombok.Setter;
//
@Getter
@Setter
public class LectureTimeRequest {
    // String으로 10m23s으로 오면 second로 계산해서 int 저장
    private Integer continueWatching;

    private boolean endFlag;
}
