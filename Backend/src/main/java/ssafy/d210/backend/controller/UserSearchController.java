package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.usersearch.UserSearchResponse;
import ssafy.d210.backend.service.UserSearchService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "UserSearchController", description = "유저 검색 API입니다.")
public class UserSearchController {
    private final UserSearchService userSearchService;

    // user email을 통한 email, nickname, name 검색
    @GetMapping("/search")
    @Operation(summary = "유저 이메일을 통한 유저 조회", description = """
            input : 유저 이메일 키워드
            output : 유저 이메일, 닉네임, 이름
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<UserSearchResponse>> searchUsers(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page") int page

    ) {
        return ResponseEntity.ok(userSearchService.searchUsers(keyword, page));
    }
}
