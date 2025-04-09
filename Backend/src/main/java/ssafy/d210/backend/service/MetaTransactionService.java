package ssafy.d210.backend.service;

import ssafy.d210.backend.dto.request.transaction.SignedRequest;

public interface MetaTransactionService {
    public boolean executeMetaTxs(SignedRequest approveRequest, SignedRequest purchaseRequest);
}
