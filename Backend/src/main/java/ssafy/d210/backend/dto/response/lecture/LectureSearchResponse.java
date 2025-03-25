package ssafy.d210.backend.dto.response.lecture;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LectureSearchResponse {
    private int totalResults;
    private int currentPage;
    private List<SearchResultResponse> searchResults = new ArrayList<>();
}
