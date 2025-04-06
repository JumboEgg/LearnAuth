package ssafy.d210.backend.service;
//
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.transaction.SignedRequest;

import java.math.BigInteger;


public interface PaymentService {
    public ResponseSuccessDto<Boolean> increaseToken(long userId, BigInteger quantity);
}
