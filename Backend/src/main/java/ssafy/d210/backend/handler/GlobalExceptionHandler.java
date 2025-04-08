package ssafy.d210.backend.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import ssafy.d210.backend.dto.common.ErrorContentDto;
import ssafy.d210.backend.dto.common.ResponseErrorDto;
import ssafy.d210.backend.exception.service.*;
import ssafy.d210.backend.util.ResponseUtil;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ResponseUtil<ErrorContentDto> responseUtil;

    @ExceptionHandler(BlockchainException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleBlockchainException(BlockchainException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DecryptedException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleDecryptedException(DecryptedException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DuplicatedValueException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleDuplicatedValue(DuplicatedValueException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EncryptedException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleEncryptedException(EncryptedException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityIsNullException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleEntityIsNull(EntityIsNullException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidLectureDataException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleInvalidLecture(InvalidLectureDataException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidQuizDataException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleInvalidQuiz(InvalidQuizDataException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRatioDataException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleInvalidRatio(InvalidRatioDataException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidSearchKeywordException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleInvalidSearchKeyword(InvalidSearchKeywordException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LectureNotFoundException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleLectureNotFound(LectureNotFoundException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PasswordIsNotAllowed.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handlePasswordNotAllowed(PasswordIsNotAllowed ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserLectureNotFoundException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleUserLectureNotFound(UserLectureNotFoundException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TokenNotInDB.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleTokenNotInDB(TokenNotInDB ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RefreshTokenNotAvailable.class)
    public ResponseEntity<ResponseErrorDto<ErrorContentDto>> handleRefreshTokenNotAvailable(RefreshTokenNotAvailable ex, WebRequest request) {
        return buildError(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }






    private ResponseEntity<ResponseErrorDto<ErrorContentDto>> buildError(Exception ex, WebRequest request, HttpStatus status) {
        ResponseErrorDto<ErrorContentDto> errorResponse = responseUtil.buildErrorResponse(
                status,
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, status);
    }


}
