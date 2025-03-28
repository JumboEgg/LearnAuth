package ssafy.d210.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
//
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCommonDto {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timeStamp;
    private int code;
    private String status;
}
