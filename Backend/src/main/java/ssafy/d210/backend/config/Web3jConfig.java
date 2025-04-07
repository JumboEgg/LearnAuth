package ssafy.d210.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import ssafy.d210.backend.contracts.CATToken;
import ssafy.d210.backend.contracts.LectureForwarder;
import ssafy.d210.backend.contracts.LectureSystem;

import java.io.IOException;
import java.math.BigInteger;

import static org.web3j.tx.gas.DefaultGasProvider.GAS_LIMIT;

@Slf4j
@Configuration
public class Web3jConfig {
    @Value("${blockchain.rpc.url}")
    private String AMOY_URL;
    @Value("${blockchain.rpc.chain-id}")
    private int CHAIN_ID;
    @Value("${blockchain.relayer.private-key}")
    private String BC_PRIVATE_KEY;
    @Value("${blockchain.forwarder.address}")
    private String BC_FORWARDER;
    @Value("${blockchain.lecture-system.address}")
    private String BC_LECTURE_SYSTEM;
    @Value("${blockchain.cat-token.address}")
    private String BC_CAT_TOKEN;

    @Bean
    public Web3j web3j() {
        try {
            Web3j web3j = Web3j.build(new HttpService(AMOY_URL));
            log.info("Connected to blockchain at {}", AMOY_URL);
            return web3j;
        } catch (Exception e) {
            log.error("Error connecting to blockchain: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to blockchain", e);
        }
    }

    @Bean
    public Credentials credentials() {
        try {
            Credentials credentials = Credentials.create(BC_PRIVATE_KEY);
            log.info("Credentials loaded successfully.");
            return credentials;
        } catch (Exception e) {
            log.error("Error loading credentials: {}", e.getMessage());
            throw new RuntimeException("Failed to load credentials", e);
        }
    }

    @Bean
    public TransactionManager web3jTransactionManager() {
        PollingTransactionReceiptProcessor processor = new PollingTransactionReceiptProcessor(
                web3j(),
                TransactionManager.DEFAULT_POLLING_FREQUENCY,
                TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH
        );
        return new RawTransactionManager(
                web3j(),
                credentials(),
                CHAIN_ID,
                processor
        );
    }

    @Bean
    public ContractGasProvider contractGasProvider() {
        return new ContractGasProvider() {
            @Override
            public BigInteger getGasPrice(String s) {
                try {
                    return web3j().ethGasPrice().send().getGasPrice();
                } catch (IOException e) {
                    return BigInteger.valueOf(20_000_000_000L);  // 20 Gwei
                }
            }

            @Override
            public BigInteger getGasPrice() {
                return getGasPrice(null);
            }

            @Override
            public BigInteger getGasLimit(String s) {
                return GAS_LIMIT;
            }

            @Override
            public BigInteger getGasLimit() {
                return getGasLimit(null);
            }
        };
    }

    @Bean
    public LectureForwarder lectureForwarder() {
        return LectureForwarder.load(
                BC_FORWARDER,
                web3j(),
                web3jTransactionManager(),
                contractGasProvider().getGasPrice(),
                contractGasProvider().getGasLimit()
        );
    }

    @Bean
    public LectureSystem lectureSystem() {
        return LectureSystem.load(
                BC_LECTURE_SYSTEM,
                web3j(),
                web3jTransactionManager(),
                contractGasProvider().getGasPrice(),
                contractGasProvider().getGasLimit()
        );
    }

    @Bean
    public CATToken catToken() {
        return CATToken.load(
                BC_CAT_TOKEN,
                web3j(),
                web3jTransactionManager(),
                contractGasProvider().getGasPrice(),
                contractGasProvider().getGasLimit()
        );
    }
}
