package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.springframework.stereotype.Service;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Numeric;
import ssafy.d210.backend.blockchain.AccountManager;
import ssafy.d210.backend.blockchain.ContractServiceFactory;
import ssafy.d210.backend.blockchain.RelayerAccount;
import ssafy.d210.backend.contracts.LectureForwarder;
import ssafy.d210.backend.dto.request.transaction.ForwardRequest;
import ssafy.d210.backend.dto.request.transaction.SignedRequest;
import ssafy.d210.backend.exception.service.BlockchainException;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaTransactionServiceImpl implements MetaTransactionService{

    private final AccountManager accountManager;
    private final ContractServiceFactory contractServiceFactory;

    @Override
    public boolean executeMetaTxs(SignedRequest approveRequest, SignedRequest purchaseRequest) {
        RelayerAccount account = null;
        try {
            account = accountManager.acquireAccount(AccountManager.OperationType.LECTURE_PURCHASE);
            LectureForwarder forwarder = contractServiceFactory.createLectureForwarder(account);

            log.info("🚀 트랜잭션 보내는 relayer address: {}", account.getAddress());

            if(executeMetaTransaction(forwarder, approveRequest))
                if(executeMetaTransaction(forwarder, purchaseRequest))
                    return true;
        } catch (Exception e) {
            log.error("Transaction failed : ", e);
        } finally {
            accountManager.releaseAccount(account, AccountManager.OperationType.LECTURE_PURCHASE);
        }
        return false;
    }

    private boolean executeMetaTransaction(LectureForwarder forwarder, SignedRequest signedRequest) {
        try {
            ForwardRequest request = signedRequest.getRequest();
            byte[] signatureBytes = Numeric.hexStringToByteArray(signedRequest.getSignature());
            byte[] dataBytes = Numeric.hexStringToByteArray(request.getData());

            // Check if deadline is valid
            BigInteger currentTime = BigInteger.valueOf(System.currentTimeMillis() / 1000);
            if (request.getDeadline().compareTo(currentTime) <= 0) {
                log.error("Request deadline has expired: current={}, deadline={}",
                        currentTime, request.getDeadline());
                throw new BlockchainException("Request deadline has expired");
            }

            // Verify nonce is correct
            BigInteger currentNonce = forwarder.nonces(request.getFrom()).send();
            BigInteger requestNonce = new BigInteger(String.valueOf(request.getNonce()));
            log.info("Current nonce for {}: {}, Request nonce: {}",
                    request.getFrom(), currentNonce, requestNonce);

            if (!currentNonce.equals(requestNonce)) {
                log.error("Nonce mismatch: current={}, request={}", currentNonce, requestNonce);
                throw new BlockchainException("Nonce mismatch: Expected " + currentNonce + " but got " + requestNonce);
            }

            LectureForwarder.ForwardRequestData requestData = new LectureForwarder.ForwardRequestData(
                    request.getFrom(),
                    request.getTo(),
                    request.getValue(),
                    request.getGas(),
                    request.getDeadline(),
                    dataBytes,
                    signatureBytes
            );

            log.info("Transaction details - From: {}, To: {}, Value: {}, Gas: {}",
                    request.getFrom(), request.getTo(), request.getValue(), request.getGas());
            log.info("Data hex: {}", request.getData());
            log.info("Signature: {}", signedRequest.getSignature());

            // Verify the request is valid
            boolean isValid = forwarder.verify(requestData).send();
            log.info("Request verification result: {}", isValid);

            if (!isValid) {
                throw new BlockchainException("Request verification failed");
            }

            // Execute the transaction
            TransactionReceipt receipt = forwarder.execute(requestData, BigInteger.ZERO).send();
            log.info("Transaction hash: {}", receipt.getTransactionHash());
            log.info("Gas used: {}", receipt.getGasUsed());
            log.info("Status: {}", receipt.getStatus());

            // Process events to verify success
            List<LectureForwarder.ExecutedForwardRequestEventResponse> events =
                    LectureForwarder.getExecutedForwardRequestEvents(receipt);

            if (events.isEmpty()) {
                log.warn("No ExecutedForwardRequest events found");
            } else {
                for (LectureForwarder.ExecutedForwardRequestEventResponse event : events) {
                    log.info("Event - Signer: {}, Nonce: {}, Success: {}",
                            event.signer, event.nonce, event.success);

                    if (!event.success) {
                        throw new BlockchainException("Forward request execution failed");
                    }
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Transaction failed: {}", e.getMessage(), e);
            throw new BlockchainException("Transaction Failed: " + e.getMessage(), e);
        }
    }
}