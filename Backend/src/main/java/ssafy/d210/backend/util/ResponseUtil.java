package ssafy.d210.backend.util;
//
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ssafy.d210.backend.dto.common.ErrorContentDto;
import ssafy.d210.backend.dto.common.ResponseErrorDto;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.enumeration.response.HereStatus;

import java.time.ZonedDateTime;
import java.util.TimeZone;

@Component
public class ResponseUtil<T> {
    public ResponseSuccessDto<T> successResponse(T data, HereStatus status) {
        ResponseSuccessDto<T> res = ResponseSuccessDto
                .<T>builder()
                .timestamp(ZonedDateTime.now(TimeZone.getTimeZone("UTC").toZoneId()))
                .code(HttpStatus.OK.value())
                .status(String.valueOf(status))
                .data(data)
                .build();
        return res;
    }

    public ResponseErrorDto<ErrorContentDto> buildErrorResponse(HttpStatus httpStatus, String message, String path) {
        ErrorContentDto errorContentDto = ErrorContentDto
                .builder()
                .message(message)
                .build();

        return ResponseErrorDto
                .<ErrorContentDto>builder()
                .timeStamp(ZonedDateTime.now(TimeZone.getTimeZone("UTC").toZoneId()))
                .code(httpStatus.value())
                .status(httpStatus.name())
                .path(path)
                .error(errorContentDto)
                .build();
    }


}
