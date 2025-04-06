package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.usersearch.UserInfoResponse;
import ssafy.d210.backend.dto.response.usersearch.UserSearchResponse;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.service.InvalidSearchKeywordException;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;

import javax.naming.directory.InvalidSearchControlsException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSearchServiceImpl implements UserSearchService{
    private final UserRepository userRepository;
    private final ResponseUtil responseUtil;

    // 검색 금지어 넣기
    private static final List<String> BLOCKED_KEYWORDS = List.of("google", "naver", "com");
    @Override
    @Transactional
    public ResponseSuccessDto<UserSearchResponse> searchUsers(String keyword, int page) {
        int pageSize = 12;

        for (String blocked : BLOCKED_KEYWORDS) {
            if (keyword.toLowerCase().contains(blocked)) {
                throw new InvalidSearchKeywordException("검색할 수 없는 키워드입니다. : ");
            }
        }

        PageRequest pageable = PageRequest.of(page - 1, pageSize);

        Page<User> userPage = findSearch(keyword, pageable);

        List<UserInfoResponse> searchResults = userPage.getContent().stream()
                .map(user -> new UserInfoResponse(user.getEmail(), user.getNickname(), user.getName()))
                .collect(Collectors.toList());

        UserSearchResponse response = new UserSearchResponse();
        response.setTotalResults((int) userPage.getTotalElements());
        response.setCurrentPage(page);
        response.setSearchResults(searchResults);

        return responseUtil.successResponse(response, HereStatus.SUCCESS_USER_SEARCH);
    }

    @Transactional(readOnly = true)
    protected Page<User> findSearch(String email, PageRequest pageable) {
        return userRepository.findByEmailContainingIgnoreCase(email, pageable);
    }
}
