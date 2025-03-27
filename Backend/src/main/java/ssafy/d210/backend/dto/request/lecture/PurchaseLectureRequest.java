package ssafy.d210.backend.dto.request.lecture;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseLectureRequest {
    private long userId;
    private long lectureId;
}
