package ssafy.d210.backend.blockchain;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class AccountManager {
    private final List<RelayerAccount> accounts = new ArrayList<>();
    private final Queue<RelayerAccount> availableAccounts = new ConcurrentLinkedQueue<>();
    private final Lock accountLock = new ReentrantLock();

    @Value("${blockchain.relayer.private-keys}")
    private List<String> privateKeys;

    @PostConstruct
    public void initialize() {
        // 1. 계정 생성
        for (int i = 0; i < privateKeys.size(); i++) {
            RelayerAccount account = new RelayerAccount(privateKeys.get(i), i);
            accounts.add(account);
            availableAccounts.add(account);
        }
    }

    public RelayerAccount acquireAccount() {
        accountLock.lock();
        try {
            RelayerAccount account = availableAccounts.poll();
            if (account == null) {
                log.warn("No available account, waiting...");
                logAccountStatus();  // 상태 로깅 추가
                // Wait for an account to become available
                return waitForAccount();
            }
            account.markInUse();
            log.info("Acquired account {}", account.getAddress());
            return account;
        } finally {
            accountLock.unlock();
        }
    }

    private RelayerAccount waitForAccount() {
        int attempts = 0;
        while (attempts < 300) {  // 5분 대기
            try {
                Thread.sleep(1000);
                accountLock.lock();
                try {
                    RelayerAccount account = availableAccounts.poll();
                    if (account != null) {
                        account.markInUse();
                        log.info("Account {} acquired after waiting {} seconds", account.getAddress(), attempts);
                        return account;
                    }

                    // 30초마다 계정 상태 로깅
                    if (attempts % 30 == 0) {
                        log.warn("Still waiting for {} account after {} seconds", attempts);
                        logAccountStatus();
                    }
                } finally {
                    accountLock.unlock();
                }
                attempts++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for account", e);
            }
        }
        log.error("Failed to acquire account after 5 minutes waiting");
        throw new RuntimeException("No account available for operation after waiting");
    }

    public void releaseAccount(RelayerAccount account) {
        if (account == null) {
            log.warn("Attempted to release null account");
            return;
        }

        accountLock.lock();
        try {
            account.markAvailable();
            availableAccounts.add(account);
            log.info("Released account {}", account.getAddress());
        } finally {
            accountLock.unlock();
        }
    }

    public void logAccountStatus() {
        accountLock.lock();
        try {
            log.info("=== Account Status ===");
            log.info("Total accounts: {}", accounts.size());

            log.info("In-use accounts:");
            for (RelayerAccount account : accounts) {
                if (account.isInUse()) {
                    long minutesInUse = (System.currentTimeMillis() - account.getLastActivityTime()) / 60000;
                    log.info("Account {}: IN USE for {} minutes",
                            account.getAddress(), minutesInUse);
                    // Log excessive use
                    if (minutesInUse > 60) {
                        log.warn("Account {} has been in use for {} minutes - this may indicate a problem",
                                account.getAddress(), minutesInUse);
                    }
                }
            }
            log.info("=====================");
        } finally {
            accountLock.unlock();
        }
    }

    @Scheduled(fixedDelay = 60000)  // 5분마다 실행
    public void scheduledCheckAccounts() {
        checkAndResetStuckAccounts(false);
    }
    public void checkAndResetStuckAccounts(boolean forceReset) {
        accountLock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            boolean foundStuckAccount = false;

            long timeoutThreshold = forceReset ? 180000 : 300000;

            for (RelayerAccount account : accounts) {
                if (account.isInUse() && account.getLastActivityTime() < currentTime - timeoutThreshold) {  // 10분 넘게 사용 중
                    log.warn("Account {} has been in use for over 10 minutes. Forcibly releasing.", account.getAddress());
                    account.markAvailable();
                    foundStuckAccount = true;

                    availableAccounts.add(account);
                    log.info("Returned account {} to pool", account.getAddress());
                }
            }

            if (foundStuckAccount) logAccountStatus();
        } finally {
            accountLock.unlock();
        }
    }


    public void resetAllAccounts() {
        accountLock.lock();
        try {
            log.warn("Forcibly resetting all account states");
            availableAccounts.clear();

            // 모든 계정 사용 가능 상태로 변경
            for (RelayerAccount account : accounts) {
                account.markAvailable();
                availableAccounts.add(account);
            }

            log.info("Account reset completed");
            logAccountStatus();
        } finally {
            accountLock.unlock();
        }
    }
}