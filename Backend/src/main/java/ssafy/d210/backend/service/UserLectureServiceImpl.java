package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.lecture.LectureTimeRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLectureServiceImpl implements UserLectureService{
    @Override
    public List<LectureResponse> getLectures(Long userId) {
        return null;
    }

    @Override
    public void updateLectureTime(Long userLectureId, Long sublectid, LectureTimeRequest request) {

    }
}
