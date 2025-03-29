package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.lecture.LectureTimeRequest;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.entity.UserLectureTime;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.repository.UserLectureTimeRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLectureServiceImpl implements UserLectureService{

    private final UserLectureRepository userLectureRepository;
    private final UserLectureTimeRepository userLectureTimeRepository;
    private final ResponseUtil responseUtil;

    @Override
    public List<UserLecture> findAllByUserId(Long userId) {
        return userLectureRepository.findAllByUserId(userId);
    }

    @Override
    public ResponseSuccessDto<Boolean> updateLectureTime(Long userLectureId, Long subLectureId, LectureTimeRequest request) {
        UserLectureTime userLectureTime = userLectureTimeRepository.findByUserLectureIdAndSubLectureId(userLectureId, subLectureId);
        userLectureTime.setContinueWatching(request.getContinueWatching());
        if (request.isEndFlag()) {
            userLectureTime.setEndFlag(1);
        }
        ResponseSuccessDto<Boolean> res = responseUtil.successResponse(true, HereStatus.SUCCESS_LECTURE_SAVEPLAYTIME);

        return res;
    }
}
