package ssafy.d210.backend.service;
//
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;


public interface PaymentService {
    public ResponseSuccessDto<Boolean> decreaseToken(long userId, int quantity);

    public ResponseSuccessDto<Boolean> increaseToken(long userId, int quantity);
}
