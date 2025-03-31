package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.category.CategoryResponse;
import ssafy.d210.backend.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Tag(name = "CategoryController", description = "카테고리 조회")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("")
    @Operation(summary = "카테고리 전체 조회", description = "전체 카테고리 목록 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<List<CategoryResponse>>> getCategoryList() {
        return ResponseEntity.ok(categoryService.getAllCategoryList());
    }
}
