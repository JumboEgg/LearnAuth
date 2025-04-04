package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;
import ssafy.d210.backend.contracts.LectureForwarder;
import ssafy.d210.backend.dto.request.transaction.ForwardRequest;
import ssafy.d210.backend.dto.request.transaction.SignedRequest;
import ssafy.d210.backend.exception.service.BlockchainException;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaTransactionServiceImpl implements MetaTransactionService{

    private final LectureForwarder lectureForwarder;

    @Override
    public boolean executeMetaTransaction(SignedRequest signedRequest) {
        ForwardRequest request = signedRequest.getRequest();
        byte[] signatureBytes = Numeric.hexStringToByteArray(signedRequest.getSignature());
        byte[] dataBytes = Numeric.hexStringToByteArray(request.getData());

        LectureForwarder.ForwardRequestData requestData = new LectureForwarder.ForwardRequestData(
                request.getFrom(),
                request.getTo(),
                request.getValue(),
                request.getGas(),
                request.getDeadline(),
                dataBytes,
                signatureBytes
        );
        try {
            log.info("Start Transaction");
            BigInteger weiValue = BigInteger.ZERO; // Value to send with the transaction
            var receipt = lectureForwarder.execute(requestData, weiValue).send();
            log.info("Transaction executed: {}", receipt);
        } catch (Exception e) {
            throw new BlockchainException("Transaction Failed", e);
        }
        return true;
    }
}
