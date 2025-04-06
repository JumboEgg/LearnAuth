package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.springframework.stereotype.Service;
import org.web3j.tx.gas.ContractGasProvider;
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

    private final Web3j web3j;
    private final ContractGasProvider gasProvider;
    private final LectureForwarder lectureForwarder;

    @Value("${blockchain.rpc.chain-id}")
    private int CHAIN_ID;
    @Value("${blockchain.relayer.private-key}")
    private String BC_PRIVATE_KEY;
    @Value("${blockchain.forwarder.address}")
    private String BC_FORWARDER;

    @Override
    public boolean executeMetaTransaction(SignedRequest signedRequest) {
        Credentials credentials = Credentials.create(BC_PRIVATE_KEY);
        TransactionManager txManager = new RawTransactionManager(web3j, credentials, CHAIN_ID);

        log.info("🚀 트랜잭션 보내는 relayer address: {}", credentials.getAddress());

        LectureForwarder forwarder = LectureForwarder.load(
                BC_FORWARDER,
                web3j,
                txManager,
                gasProvider.getGasPrice(),
                gasProvider.getGasLimit()
        );

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
            var receipt = forwarder.execute(requestData, weiValue).send();
            log.info("Transaction executed: {}", receipt);
        } catch (Exception e) {
            throw new BlockchainException("Transaction Failed", e);
        }
        return true;
    }
}
