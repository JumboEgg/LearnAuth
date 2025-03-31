package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.category.CategoryListResponse;
import ssafy.d210.backend.dto.response.category.CategoryResponse;
import ssafy.d210.backend.entity.Category;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.CategoryRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final ResponseUtil responseUtil;

    @Override
    public ResponseSuccessDto<CategoryListResponse> getCategoryList() {
        //DB 카테고리 전체 조회
        List<Category> categories = categoryRepository.findAll();
        // Category entity -> CategoryResponse DTO
        List<CategoryResponse> responseList = categories.stream()
                .map(cat -> new CategoryResponse(cat.getId(), cat.getCategoryName().name()))
                .collect(Collectors.toList());
        // CategoryListResponse에 담기
        CategoryListResponse categoryListResponse = new CategoryListResponse();
        categoryListResponse.setCategoryList(responseList);

        return responseUtil.successResponse(categoryListResponse, HereStatus.SUCCESS_CATEGORY_LIST);
    }
}
