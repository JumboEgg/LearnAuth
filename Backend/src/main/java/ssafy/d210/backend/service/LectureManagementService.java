package ssafy.d210.backend.service;
//
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.lecture.LectureRegisterRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;


public interface LectureManagementService {

    ResponseSuccessDto<Boolean> registerLecture(LectureRegisterRequest request);
}
