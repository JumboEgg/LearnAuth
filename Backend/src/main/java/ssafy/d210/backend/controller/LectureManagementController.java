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
}
