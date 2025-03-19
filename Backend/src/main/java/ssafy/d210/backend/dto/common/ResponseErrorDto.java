package ssafy.d210.backend.dto.common;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper=false)
public class ResponseErrorDto<T> extends ResponseCommonDto {

    private String path;
    private T error;

    @Builder
    public ResponseErrorDto(ZonedDateTime timeStamp, int code, String status, String path, T error) {
        super(timeStamp, code, status);
        this.path = path;
        this.error = error;
    }

}
