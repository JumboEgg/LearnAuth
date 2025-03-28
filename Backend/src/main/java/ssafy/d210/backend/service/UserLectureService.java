package ssafy.d210.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.lecture.LectureTimeRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.entity.UserLecture;

import java.util.ArrayList;
import java.util.List;

// 사용자가 수강한 강의 목록 조회, 시청 시간 업데이트

public interface UserLectureService {
    public List<UserLecture> findAllByUserId(Long userId);

    // 강의 재생 시간 저장
    ResponseSuccessDto<Object> updateLectureTime(Long userLectureId, Long subLectureId, LectureTimeRequest request);
}
