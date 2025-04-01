package ssafy.d210.backend.service;


import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.usersearch.UserSearchResponse;

public interface UserSearchService {
    // 유저 검색 기능
    ResponseSuccessDto<UserSearchResponse> searchUsers(String keyword, int page);
}
