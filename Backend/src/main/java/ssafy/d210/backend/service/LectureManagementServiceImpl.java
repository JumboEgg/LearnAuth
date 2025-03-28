package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.lecture.LectureRegisterRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureManagementServiceImpl implements LectureManagementService {

    @Override
    public LectureResponse registerLecture(LectureRegisterRequest request) {
        return null;
    }
}
