package ssafy.d210.backend.service;

import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.lecture.LectureDetailResponse;
import ssafy.d210.backend.dto.response.lecture.LectureInfoResponse;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.dto.response.lecture.RecommendedLectureResponse;

import java.util.List;

// 전체 강의 조회, 추천 강의, 상세 조회, 검색 기능

public interface LectureService {

    // 카테고리별 강의 조회
    public ResponseSuccessDto<List<LectureInfoResponse>> getLecturesByCategory(String category, int page);

    // 메인 화면 강의 목록
    public ResponseSuccessDto<RecommendedLectureResponse> getRecommendedLectures();

    // 강의 상세 조회
    public ResponseSuccessDto<LectureDetailResponse> getLectureDetail(Long lectureId, Long userId);

    // 강의 검색(검색어)
    public ResponseSuccessDto<List<LectureInfoResponse>> searchLectures(String keyword, int page);

    // 강의 구매
    // 정은이 화이팅!
    public ResponseSuccessDto<Object> purchaseLecture(Long userId, Long lectureId);

    // 보유 강의 목록 조회
    public ResponseSuccessDto<List<LectureResponse>> getPurchasedLectures(Long userId);

    public ResponseSuccessDto<List<LectureResponse>> getParticipatedLectures(Long userId);
}
