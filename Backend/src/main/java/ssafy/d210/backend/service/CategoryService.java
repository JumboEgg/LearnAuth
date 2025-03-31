package ssafy.d210.backend.service;

import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.category.CategoryResponse;

import java.util.List;

// 카테고리 조회 로직
public interface CategoryService {
    public ResponseSuccessDto<List<CategoryResponse>> getAllCategoryList();
}
