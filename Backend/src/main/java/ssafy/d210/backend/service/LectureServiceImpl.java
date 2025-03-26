package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.dto.response.lecture.SearchResultResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService{
    @Override
    public List<LectureResponse> getAllLectures() {
        return null;
    }

    @Override
    public List<LectureResponse> getRecommendedLectures() {
        return null;
    }

    @Override
    public LectureResponse getLectureDetail(Long lectureId, Long userId) {
        return null;
    }

    @Override
    public SearchResultResponse searchLectures(String keyword, int page) {
        return null;
    }
}
