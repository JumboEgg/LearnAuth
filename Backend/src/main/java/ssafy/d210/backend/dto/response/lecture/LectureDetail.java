package ssafy.d210.backend.dto.response.lecture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LectureDetail {
    private long lectureId;
    private String title;
    private int price;
    private String goal;
    private String description;
    private String lecturer;
    private String lectureUrl;
    private String categoryName;
    private long reportId;
    private String CID;
}
