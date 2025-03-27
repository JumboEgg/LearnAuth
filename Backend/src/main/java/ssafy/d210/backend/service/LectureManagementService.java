package ssafy.d210.backend.service;

import ssafy.d210.backend.dto.request.lecture.LectureRegisterRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;

// 우리는 강의 수정 삭제 없음

public interface LectureManagementService {

    // 강의 등록하기
    public LectureResponse registerLecture(LectureRegisterRequest request);

    // "강의 등록 이메일 찾기" 는 "회원 가입 이메일 중복 확인"입니다.
}
