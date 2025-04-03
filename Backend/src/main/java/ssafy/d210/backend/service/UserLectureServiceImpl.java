package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.lecture.LectureTimeRequest;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.entity.UserLectureTime;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.service.UserLectureNotFoundException;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.repository.UserLectureTimeRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
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
        userLectureTimeRepository.save(userLectureTime);
        ResponseSuccessDto<Boolean> res = responseUtil.successResponse(true, HereStatus.SUCCESS_LECTURE_SAVEPLAYTIME);

        return res;
    }

    @Override
    public ResponseSuccessDto<Object> updateLastViewedLecture(Long userLectureId, Long subLectureId) {
        Optional<UserLecture> optional = userLectureRepository.findById(userLectureId);

        if (optional.isEmpty()) {
            throw new UserLectureNotFoundException("해당 userLectureId가 존재하지 않습니다." + userLectureId);

        }

        UserLecture userLecture = optional.get();
        userLecture.setRecentLectureId(subLectureId);
        userLectureRepository.save(userLecture);

        return responseUtil.successResponse(null, HereStatus.SUCCESS_LECTURE_UPDATE);
    }
}
