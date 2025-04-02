package ssafy.d210.backend.dto.response.usersearch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserSearchResponse {
    private int totalResults;
    private int currentPage;
    private List<UserInfoResponse> searchResults = new ArrayList<>();
}
