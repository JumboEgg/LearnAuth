package ssafy.d210.backend.dto.response.lecture;
//
import lombok.*;

@Getter
@Setter
public class SubLectureDetailResponse {
    private long subLectureId;
    private String subLectureTitle;
    private String lectureUrl;
    private int lectureLength;
    private Long lectureOrder;
    private Integer continueWatching;
    private Boolean endFlag;

    public SubLectureDetailResponse(long subLectureId, String subLectureTitle, String lectureUrl, int lectureLength, Long  lectureOrder, Integer continueWatching, Integer endFlag) {
        this.subLectureId = subLectureId;
        this.subLectureTitle = subLectureTitle;
        this.lectureUrl = lectureUrl;
        this.lectureLength = lectureLength;
        this.lectureOrder = lectureOrder;
        this.continueWatching = continueWatching;
        this.endFlag = endFlag != null && endFlag == 1;
    }
}
