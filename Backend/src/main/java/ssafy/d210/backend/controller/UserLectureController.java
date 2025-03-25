package ssafy.d210.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.service.UserLectureService;

import java.util.List;

@RestController
@RequestMapping("/api/userlecture")
@RequiredArgsConstructor
public class UserLectureController {

    private final UserLectureService userLectureService;

    // 다 뜯어야...
    @GetMapping
    public ResponseEntity<List<LectureResponse>> getUserLectures(@RequestParam Long userId) {
        List<LectureResponse> lectures = userLectureService.getLectures(userId);
        return ResponseEntity.ok(lectures);
    }

// userlecture로 시작하는 url 중 patch 하는게 있는데 의존성 추가 해야 사용 가능
//    @patchMapping("/{userLectureId}/time")
}
