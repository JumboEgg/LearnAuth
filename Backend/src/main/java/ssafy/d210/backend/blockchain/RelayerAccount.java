package ssafy.d210.backend.blockchain;

import lombok.Getter;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;

public class RelayerAccount {
    private final String privateKey;
    private final int index;
    private final Credentials credentials;
    private volatile boolean inUse = false;
    @Getter
    private long lastActivityTime = 0;
    private BigInteger lastKnownNonce = BigInteger.valueOf(-1);

    public RelayerAccount(String privateKey, int index) {
        this.privateKey = privateKey;
        this.index = index;
        this.credentials = Credentials.create(privateKey);
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public int getIndex() {
        return index;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getAddress() {
        return credentials.getAddress();
    }

    public void markInUse() {
        this.inUse = true;
    }

    public void markAvailable() {
        this.inUse = false;
    }

    public boolean isInUse() {
        return inUse;
    }

    public synchronized BigInteger getAndIncrementNonce(Web3j web3j) {
        try {
            if (lastKnownNonce.compareTo(BigInteger.valueOf(-1)) == 0) {
                // Initialize nonce from blockchain
                lastKnownNonce = web3j.ethGetTransactionCount(
                        credentials.getAddress(), org.web3j.protocol.core.DefaultBlockParameterName.PENDING
                ).send().getTransactionCount();
            } else {
                // Increment the last known nonce
                lastKnownNonce = lastKnownNonce.add(BigInteger.ONE);
            }
            return lastKnownNonce;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get nonce for account: " + credentials.getAddress(), e);
        }
    }
}