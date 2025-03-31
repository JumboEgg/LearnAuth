package ssafy.d210.backend.service;
//
import ssafy.d210.backend.dto.request.lecture.LectureRegisterRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;


public interface LectureManagementService {

    // 강의 등록하기
    // public LectureResponse registerLecture(LectureRegisterRequest request);
    // 한나 : 200ok true, false 반환 문제 없으면 위 주석 지우기
    boolean registerLecture(LectureRegisterRequest request);

    // "강의 등록 이메일 찾기" 는 "회원 가입 이메일 중복 확인"입니다.
}
