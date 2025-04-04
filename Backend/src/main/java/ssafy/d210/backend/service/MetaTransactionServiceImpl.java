package ssafy.d210.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.transaction.SignedRequest;

@Slf4j
@Service
public class MetaTransactionServiceImpl implements MetaTransactionService{

    @Override
    public boolean executeMetaTransaction(SignedRequest request) {
        return true;
    }
}
