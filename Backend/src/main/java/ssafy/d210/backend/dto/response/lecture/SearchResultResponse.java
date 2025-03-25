package ssafy.d210.backend.dto.response.lecture;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResultResponse {
    private long lectureId;
    private String title;
    private String categoryName;
    private int price;
    private String thumbnailUrl;
}
