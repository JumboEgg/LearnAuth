package ssafy.d210.backend.blockchain;

import lombok.Getter;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;

public class RelayerAccount {
    private final String privateKey;
    private final int index;
    private final Credentials credentials;
    @Getter
    private volatile boolean inUse = false;
    @Getter
    private long lastActivityTime;
    private BigInteger lastKnownNonce = BigInteger.valueOf(-1);

    public RelayerAccount(String privateKey, int index) {
        this.privateKey = privateKey;
        this.index = index;
        this.credentials = Credentials.create(privateKey);
        this.lastActivityTime = System.currentTimeMillis();
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getAddress() {
        return credentials.getAddress();
    }

    public void markInUse() {
        markInUse(System.currentTimeMillis());
    }

    public void markInUse(long timestamp) {
        this.inUse = true;
        this.lastActivityTime = timestamp;
    }

    public void markAvailable() {
        this.inUse = false;
        this.lastActivityTime = System.currentTimeMillis();
    }
}