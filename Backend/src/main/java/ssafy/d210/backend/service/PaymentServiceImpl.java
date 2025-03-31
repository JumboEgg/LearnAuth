package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{
    @Override
    public ResponseSuccessDto<Boolean> decreaseToken(long userId, int quantity) {
        return null;
    }

    @Override
    public ResponseSuccessDto<Boolean> increaseToken(long userId, int quantity) {
        return null;
    }


}
