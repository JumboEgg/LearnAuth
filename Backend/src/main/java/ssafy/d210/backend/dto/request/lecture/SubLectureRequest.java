package ssafy.d210.backend.dto.request.lecture;

import lombok.Getter;
import lombok.Setter;
//
@Getter
@Setter
public class SubLectureRequest {
    private String subLectureTitle;
    private String subLectureUrl;
    private int subLectureLength;

    public String getSubLectureTitle() { return subLectureTitle; }
    public void setSubLectureTitle(String subLectureTitle) { this.subLectureTitle = subLectureTitle; }
    public String getSubLectureUrl() { return subLectureUrl; }
    public void setSubLectureUrl(String subLectureUrl) { this.subLectureUrl = subLectureUrl; }
    public int getSubLectureLength() { return subLectureLength; }
    public void setSubLectureLength(int subLectureLength) { this.subLectureLength = subLectureLength; }
}
