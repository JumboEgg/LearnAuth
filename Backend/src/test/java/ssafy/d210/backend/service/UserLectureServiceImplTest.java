package ssafy.d210.backend.service;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
//import ssafy.d210.backend.dto.common.ResponseSuccessDto;
//import ssafy.d210.backend.dto.request.lecture.LectureTimeRequest;
//import ssafy.d210.backend.entity.UserLecture;
//import ssafy.d210.backend.entity.UserLectureTime;
//import ssafy.d210.backend.enumeration.response.HereStatus;
//import ssafy.d210.backend.exception.DefaultException;
//import ssafy.d210.backend.exception.service.UserLectureNotFoundException;
//import ssafy.d210.backend.repository.UserLectureRepository;
//import ssafy.d210.backend.repository.UserLectureTimeRepository;
//import ssafy.d210.backend.util.ResponseUtil;
//
//import javax.swing.text.html.Option;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
@ExtendWith(MockitoExtension.class)
class UserLectureServiceImplTest {
//
//    @Mock private UserLectureRepository userLectureRepository;
//    @Mock private UserLectureTimeRepository userLectureTimeRepository;
//    @Mock private ResponseUtil responseUtil;
//
//    @InjectMocks private UserLectureServiceImpl userLectureService;
//
//    @Test
//    @DisplayName("유저 ID로 수강 강의 리스트 조회 성공")
//    void findAllByUserIdSuccess() {
//        // given
//        Long userId = 1L;
//        List<UserLecture> lectures = List.of(new UserLecture(), new UserLecture());
//        when(userLectureRepository.findAllByUserId(userId)).thenReturn(lectures);
//
//        // when
//        List<UserLecture> result = userLectureService.findAllByUserId(userId);
//
//        // then
//        assertThat(result).hasSize(2);
//        verify(userLectureRepository, times(1)).findAllByUserId(userId);
//    }
//
//    @Test
//    @DisplayName("이어보기 시간 저장 성공")
//    void updateLectureTimeSuccess() {
//        // given
//        Long userLectureId = 1L;
//        Long subLectureId = 2L;
//
//        LectureTimeRequest request = new LectureTimeRequest();
//        request.setContinueWatching(120);
//        request.setEndFlag(true);
//
//        UserLectureTime ult = new UserLectureTime();
//        when(userLectureTimeRepository.findByUserLectureIdAndSubLectureId(userLectureId, subLectureId)).thenReturn(ult);
//
//        ResponseSuccessDto<Boolean> responseDto = new ResponseSuccessDto<>();
//        responseDto.setData(true);
//        responseDto.setStatus(HereStatus.SUCCESS_LECTURE_SAVEPLAYTIME.name());
//
//        when(responseUtil.successResponse(true, HereStatus.SUCCESS_LECTURE_SAVEPLAYTIME)).thenReturn(responseDto);
//
//        // when
//        ResponseSuccessDto<Boolean> result = userLectureService.updateLectureTime(userLectureId, subLectureId, request);
//
//        // then
//        assertThat(result.getData()).isTrue();
//        assertThat(result.getStatus()).isEqualTo(HereStatus.SUCCESS_LECTURE_SAVEPLAYTIME.name());
//        assertThat(ult.getContinueWatching()).isEqualTo(120L);
//        assertThat(ult.getEndFlag()).isEqualTo(1);
//        verify(userLectureTimeRepository).save(ult);
//    }
//
//    @Test
//    @DisplayName("최근 강의 업데이트 성공")
//    void updateLastViewedLectureSuccess() {
//        // given
//        Long userLectureId = 1L;
//        Long subLectureId = 5L;
//
//        UserLecture ul = new UserLecture();
//        when(userLectureRepository.findById(userLectureId)).thenReturn(Optional.of(ul));
//
//        ResponseSuccessDto<Object> responseDto = new ResponseSuccessDto<>();
//        responseDto.setData(null);
//        responseDto.setStatus(HereStatus.SUCCESS_LECTURE_UPDATE.name());
//
//        when(responseUtil.successResponse(null, HereStatus.SUCCESS_LECTURE_UPDATE)).thenReturn(responseDto);
//
//        // when
//        ResponseSuccessDto<Object> result = userLectureService.updateLastViewedLecture(userLectureId, subLectureId);
//
//        // then
//        assertThat(result.getStatus()).isEqualTo(HereStatus.SUCCESS_LECTURE_UPDATE.name());
//        assertThat(ul.getRecentLectureId()).isEqualTo(subLectureId);
//        verify(userLectureRepository).save(ul);
//    }
//
//    @Test
//    @DisplayName("최근 강의 업데이트 실패 - userLectureid 없음")
//    void updateLastViewedLectureFailWhenNotFound() {
//        // given
//        Long userLectureId = 1L;
//        Long subLectureId = 5L;
//
//        when(userLectureRepository.findById(userLectureId)).thenReturn(Optional.empty());
//
//        // when & then
//        assertThrows(UserLectureNotFoundException.class,
//                () -> userLectureService.updateLastViewedLecture(userLectureId, subLectureId));
//    }
//
//    @Test
//    @DisplayName("재생 시간 업데이트 실패 - UserLectureTime이 존재하지 않는다.")
//    void updateLectureTime_fail_userLectureTimeNotFound() {
//        Long userLectureId = 1L;
//        Long subLectureId = 2L;
//
//        LectureTimeRequest request = new LectureTimeRequest();
//        request.setContinueWatching(120);
//        request.setEndFlag(false);
//
//        when(userLectureTimeRepository.findByUserLectureIdAndSubLectureId(userLectureId, subLectureId))
//                .thenReturn(null);
//
//        assertThrows(NullPointerException.class,
//                () -> userLectureService.updateLectureTime(userLectureId, subLectureId, request));
//
//    }
//
//    @Test
//    @DisplayName("최근 강의 업데이트 실패 - subLectureId가 null")
//    void updateLastViewedLecture_fail_nullSubLectureId() {
//        // given
//        Long userLectureId = 1L;
//        Long subLectureId = null;
//
//        UserLecture userLecture = new UserLecture();
//        userLecture.setRecentLectureId(5L);
//
//        when(userLectureRepository.findById(userLectureId)).thenReturn(Optional.of(userLecture));
//
//        assertThrows(DefaultException.class, () -> {
//            userLectureService.updateLastViewedLecture(userLectureId, subLectureId);
//        });
//    }
//
//
}