package ssafy.d210.backend.service;

import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.dto.response.lecture.SearchResultResponse;

import java.util.ArrayList;
import java.util.List;

// 전체 강의 조회, 추천 강의, 상세 조회, 검색 기능

public interface LectureService {

    // response 이거 여기꺼 맞는지 다 확인 필요
    // 전체 강의 목록 조회
    public List<LectureResponse> getAllLectures();

    // 조건에 따른 강의 조회
    public List<LectureResponse> getRecommendedLectures();

    // 강의 상세 조회
    public LectureResponse getLectureDetail(Long lectureId, Long userId);

    // 강의 검색
    public SearchResultResponse searchLectures(String keyword, int page);

    // 강의 구매
    // 정은이 화이팅!
}
