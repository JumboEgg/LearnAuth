package ssafy.d210.backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
//
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseSuccessDto<T> extends ResponseCommonDto {

    private T data;

    @Builder
    public ResponseSuccessDto(ZonedDateTime timestamp, int code, String status, T data) {
        super(timestamp, code, status);
        this.data = data;
    }
}
