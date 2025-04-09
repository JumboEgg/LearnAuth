package ssafy.d210.backend.blockchain;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import ssafy.d210.backend.contracts.CATToken;
import ssafy.d210.backend.contracts.LectureForwarder;
import ssafy.d210.backend.contracts.LectureSystem;

@Service
@RequiredArgsConstructor
public class ContractServiceFactory {
    private final Web3j web3j;
    private final ContractGasProvider gasProvider;
    private final AccountManager accountManager;

    @Value("${blockchain.rpc.chain-id}")
    private int CHAIN_ID;
    @Value("${blockchain.forwarder.address}")
    private String BC_FORWARDER;
    @Value("${blockchain.lecture-system.address}")
    private String BC_LECTURE_SYSTEM;
    @Value("${blockchain.cat-token.address}")
    private String BC_CAT_TOKEN;

    public LectureForwarder createLectureForwarder(RelayerAccount account) {
        TransactionManager txManager = createTransactionManager(account);
        return LectureForwarder.load(
                BC_FORWARDER,
                web3j,
                txManager,
                gasProvider
        );
    }

    public LectureSystem createLectureSystem(RelayerAccount account) {
        TransactionManager txManager = createTransactionManager(account);
        return LectureSystem.load(
                BC_LECTURE_SYSTEM,
                web3j,
                txManager,
                gasProvider
        );
    }

    public CATToken createCATToken(RelayerAccount account) {
        TransactionManager txManager = createTransactionManager(account);
        return CATToken.load(
                BC_CAT_TOKEN,
                web3j,
                txManager,
                gasProvider
        );
    }

    private TransactionManager createTransactionManager(RelayerAccount account) {
        PollingTransactionReceiptProcessor processor = new PollingTransactionReceiptProcessor(
                web3j,
                TransactionManager.DEFAULT_POLLING_FREQUENCY,
                TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH
        );
        return new RawTransactionManager(
                web3j,
                account.getCredentials(),
                CHAIN_ID,
                processor
        );
    }
}