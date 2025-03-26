package ssafy.d210.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.service.LectureManagementService;

@RestController
@RequestMapping("/api/lecture")
@RequiredArgsConstructor
public class LectureManagementController {

    private final LectureManagementService lectureManagementService;

    // 강의 등록하기 @PostMapping

    // "강의 등록 이메일 찾기" 는 "회원 가입 이메일 중복 확인"입니다.
}
