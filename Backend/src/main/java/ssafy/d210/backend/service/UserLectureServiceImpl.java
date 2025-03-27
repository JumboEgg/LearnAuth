package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.lecture.LectureTimeRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.exception.DefaultException;
import ssafy.d210.backend.repository.UserLectureRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLectureServiceImpl implements UserLectureService{
    private final UserLectureRepository userLectureRepository;

    @Override
    public void updateLectureTime(Long userLectureId, Long sublectid, LectureTimeRequest request) {

    }

    @Override
    public List<UserLecture> findAllByUserId(Long userId) {
        return userLectureRepository.findAllByUserId(userId);
    }
}
