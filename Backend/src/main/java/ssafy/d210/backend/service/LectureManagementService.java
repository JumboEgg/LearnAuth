package ssafy.d210.backend.service;

import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.lecture.LectureRegisterRequest;
import ssafy.d210.backend.dto.request.lecture.LectureRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;

// 우리는 강의 수정 삭제 없음
@Service
public interface LectureManagementService {

    // 강의 등록 로직 구현
    public LectureResponse registerLecture(LectureRegisterRequest request);

}
