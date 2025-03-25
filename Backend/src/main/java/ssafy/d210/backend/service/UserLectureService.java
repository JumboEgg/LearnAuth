package ssafy.d210.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.lecture.LectureTimeRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;

import java.util.ArrayList;
import java.util.List;

// 사용자가 수강한 강의 목록 조회, 시청 시간 업데이트
@Service
public interface UserLectureService {

    // LectureResponse는 userlecture 말고도 쓰기 때문에 그냥 Lecture Response라 한다.
    public List<LectureResponse> getLectures(Long userId);
    public void updateLectureTime(Long userLectureId, Long sublectid, LectureTimeRequest request);
}
