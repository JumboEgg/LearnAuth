package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.lecture.LectureDetailResponse;
import ssafy.d210.backend.dto.response.lecture.LectureInfoResponse;
import ssafy.d210.backend.dto.response.lecture.RecommendedLectureResponse;
import ssafy.d210.backend.repository.LectureRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService{
    private final LectureRepository lectureRepository;

    @Override
    public ResponseSuccessDto<List<LectureInfoResponse>> getLecturesByCategory(String category, int page) {
        return null;
    }

    @Override
    public ResponseSuccessDto<RecommendedLectureResponse> getRecommendedLectures() {
        return null;
    }

    @Override
    public ResponseSuccessDto<LectureDetailResponse> getLectureDetail(Long lectureId, Long userId) {
        return null;
    }

    @Override
    public ResponseSuccessDto<List<LectureInfoResponse>> searchLectures(String keyword, int page) {
        return null;
    }

    @Override
    public ResponseSuccessDto<Boolean> purchaseLecture(Long userId, Long lectureId) {
        return null;
    }

    @Override
    public ResponseSuccessDto<List<LectureInfoResponse>> getPurchasedLectures(Long userId) {
        return null;
    }

    @Override
    public ResponseSuccessDto<List<LectureInfoResponse>> getParticipatedLectures(Long userId) {
        return null;
    }
}
