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
    // 내가 보유, 참여한 강의 @GetMapping

    // 재생 시간 업데이트 @PostMapping

}
