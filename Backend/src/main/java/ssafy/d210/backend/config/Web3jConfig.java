package ssafy.d210.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;

import java.io.IOException;
import java.math.BigInteger;

import static org.web3j.tx.gas.DefaultGasProvider.GAS_LIMIT;

@Slf4j
@Configuration
public class Web3jConfig {
    @Value("${blockchain.rpc.url}")
    private String AMOY_URL;

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
}
