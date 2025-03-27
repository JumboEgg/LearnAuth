package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.lecture.LectureDetailResponse;
import ssafy.d210.backend.dto.response.lecture.LectureInfoResponse;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.dto.response.lecture.RecommendedLectureResponse;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.LectureRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService{
    private final LectureRepository lectureRepository;
    private final ResponseUtil responseUtil;

    @Override
    public ResponseSuccessDto<List<LectureInfoResponse>> getLecturesByCategory(String category, int page) {
        // 카테고리별 강의 목록 조회
        int offset = (page - 1) * 12;
        List<LectureInfoResponse> lectures = lectureRepository.getLecturesByCategory(category, offset);
        log.info("Category {} page {} lectures: {}", category, page, lectures);

        // 결과 검증 및 로깅
        if (lectures.isEmpty()) {
            log.warn("No lectures found for category {}", category);
        }

        ResponseSuccessDto<List<LectureInfoResponse>> res = responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE_CATEGORY);
        return res;
    }

    @Override
    public ResponseSuccessDto<RecommendedLectureResponse> getRecommendedLectures() {

        RecommendedLectureResponse lectures = new RecommendedLectureResponse();
        List<LectureInfoResponse> mostCompletedLectures = lectureRepository.getMostFinishedLectures();
        List<LectureInfoResponse> randomLectures = lectureRepository.getRandomLectures();
        List<LectureInfoResponse> recentLectures = lectureRepository.getNewestLectures();

        lectures.setMostCompletedLectures(mostCompletedLectures);
        lectures.setRandomLectures(randomLectures);
        lectures.setRecentLectures(recentLectures);

        ResponseSuccessDto<RecommendedLectureResponse> res = responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE);
        return res;
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
    public ResponseSuccessDto<List<LectureResponse>> getPurchasedLectures(Long userId) {
        // 사용자 ID로 구매한 강의 목록 조회
        List<LectureResponse> lectures = lectureRepository.getPurchasedLectures(userId);
        log.info("UserId: {} lectures: {}", userId, lectures);

        // 결과 검증 및 로깅
        if (lectures.isEmpty()) {
            log.warn("No lectures found for userId {}", userId);
        }

        ResponseSuccessDto<List<LectureResponse>> res = responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE);
        return res;
    }

    @Override
    public ResponseSuccessDto<List<LectureResponse>> getParticipatedLectures(Long userId) {
        // 사용자 ID로 구매한 강의 목록 조회
        List<LectureResponse> lectures = lectureRepository.getParticipatedLectures(userId);
        log.info("UserId: {} lectures: {}", userId, lectures);

        // 결과 검증 및 로깅
        if (lectures.isEmpty()) {
            log.warn("No lectures found for userId {}", userId);
        }

        ResponseSuccessDto<List<LectureResponse>> res = responseUtil.successResponse(lectures, HereStatus.SUCCESS_LECTURE);
        return res;
    }
}
