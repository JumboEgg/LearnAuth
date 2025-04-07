package ssafy.d210.backend.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import ssafy.d210.backend.dto.common.ErrorContentDto;
import ssafy.d210.backend.dto.common.ResponseErrorDto;
import ssafy.d210.backend.exception.service.BlockchainException;
import ssafy.d210.backend.util.ResponseUtil;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ResponseUtil<ErrorContentDto> responseUtil;

    @ExceptionHandler(BlockchainException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleBlockchainException(BlockchainException ex, WebRequest request) {
        ResponseErrorDto<ErrorContentDto> errorResponse = responseUtil.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
