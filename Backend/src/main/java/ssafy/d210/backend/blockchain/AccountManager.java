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
    private final Map<OperationType, Queue<RelayerAccount>> availableAccounts = new ConcurrentHashMap<>();
    private final Lock accountLock = new ReentrantLock();

    @Value("${blockchain.relayer.private-keys}")
    private List<String> privateKeys;

    public enum OperationType {
        REGISTRATION,
        LECTURE_PURCHASE,
        TOKEN_DISTRIBUTION
    }

    @PostConstruct
    public void initialize() {
        // 1. 계정 생성
        for (int i = 0; i < privateKeys.size(); i++) {
            RelayerAccount account = new RelayerAccount(privateKeys.get(i), i);
            accounts.add(account);
            log.debug("Added Account: {}", account.getAddress());
        }

        // 2. 큐 초기화
        for (OperationType type : OperationType.values()) {
            availableAccounts.put(type, new ConcurrentLinkedQueue<>());
        }

        // 3. 계정 할당
        assignAccountsToOperations();

        // 4. 초기화 완료 로그
        log.info("AccountManager initialized with {} accounts", accounts.size());
        for (OperationType type : OperationType.values()) {
            log.info("Operation {}: {} accounts available",
                    type, availableAccounts.get(type).size());
        }
    }

    private void assignAccountsToOperations() {
        int index = 0;

        // Assign 2 accounts to user registration & lecture registration
        availableAccounts.get(OperationType.REGISTRATION).add(accounts.get(index++));
        availableAccounts.get(OperationType.REGISTRATION).add(accounts.get(index++));

        // Assign 2 accounts to lecture purchase (meta transactions)
        availableAccounts.get(OperationType.LECTURE_PURCHASE).add(accounts.get(index++));
        availableAccounts.get(OperationType.LECTURE_PURCHASE).add(accounts.get(index++));

        // Assign 2 accounts to token distribution
        availableAccounts.get(OperationType.TOKEN_DISTRIBUTION).add(accounts.get(index++));
        availableAccounts.get(OperationType.TOKEN_DISTRIBUTION).add(accounts.get(index));
    }

    public RelayerAccount acquireAccount(OperationType type) {
        accountLock.lock();
        try {
            Queue<RelayerAccount> queue = availableAccounts.get(type);
            RelayerAccount account = queue.poll();
            if (account == null) {
                log.warn("No available account for operation type {}, waiting...", type);
                logAccountStatus();  // 상태 로깅 추가
                // Wait for an account to become available
                return waitForAccount(type);
            }
            account.markInUse();
            log.info("Acquired account {} for operation {}", account.getAddress(), type);
            return account;
        } finally {
            accountLock.unlock();
        }
    }

    private RelayerAccount waitForAccount(OperationType type) {
        int attempts = 0;
        while (attempts < 300) {  // 5분 대기
            try {
                Thread.sleep(1000);
                accountLock.lock();
                try {
                    Queue<RelayerAccount> queue = availableAccounts.get(type);
                    RelayerAccount account = queue.poll();
                    if (account != null) {
                        account.markInUse();
                        log.info("Account {} acquired after waiting {} seconds", account.getAddress(), attempts);
                        return account;
                    }

                    // 30초마다 계정 상태 로깅
                    if (attempts % 30 == 0) {
                        log.warn("Still waiting for {} account after {} seconds", type, attempts);
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
        log.error("Failed to acquire account for {} after 5 minutes waiting", type);
        throw new RuntimeException("No account available for operation " + type + " after waiting");
    }

    public void releaseAccount(RelayerAccount account, OperationType type) {
        if (account == null) {
            log.warn("Attempted to release null account for type {}", type);
            return;
        }

        accountLock.lock();
        try {
            account.markAvailable();
            availableAccounts.get(type).add(account);
            log.info("Released account {} back to {} pool", account.getAddress(), type);
        } finally {
            accountLock.unlock();
        }
    }

    public void logAccountStatus() {
        accountLock.lock();
        try {
            log.info("=== Account Status ===");
            log.info("Total accounts: {}", accounts.size());

            for (OperationType type : OperationType.values()) {
                Queue<RelayerAccount> queue = availableAccounts.get(type);
                log.info("Operation {}: {} accounts available", type, queue.size());
            }

            log.info("In-use accounts:");
            for (RelayerAccount account : accounts) {
                if (account.isInUse()) {
                    log.info("Account {}: IN USE for {} minutes",
                            account.getAddress(),
                            (System.currentTimeMillis() - account.getLastActivityTime()) / 60000);
                }
            }
            log.info("=====================");
        } finally {
            accountLock.unlock();
        }
    }

    @Scheduled(fixedDelay = 300000)  // 5분마다 실행
    public void checkAndResetStuckAccounts() {
        accountLock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            boolean foundStuckAccount = false;

            for (RelayerAccount account : accounts) {
                if (account.isInUse() && account.getLastActivityTime() < currentTime - 600000) {  // 10분 넘게 사용 중
                    log.warn("Account {} has been in use for over 10 minutes. Forcibly releasing.", account.getAddress());
                    account.markAvailable();
                    foundStuckAccount = true;

                    // 적절한 풀에 계정 재할당
                    for (OperationType type : OperationType.values()) {
                        if (getAccountCountForType(type) < getDesiredAccountCountForType(type)) {
                            availableAccounts.get(type).add(account);
                            log.info("Returned account {} to {} pool", account.getAddress(), type);
                            break;
                        }
                    }
                }
            }

            if (foundStuckAccount) {
                logAccountStatus();
            }
        } finally {
            accountLock.unlock();
        }
    }

    private int getAccountCountForType(OperationType type) {
        return availableAccounts.get(type).size();
    }

    private int getDesiredAccountCountForType(OperationType type) {
        switch (type) {
            case REGISTRATION:
                return 2;
            case LECTURE_PURCHASE:
                return 2;
            case TOKEN_DISTRIBUTION:
                return 2;
            default:
                return 0;
        }
    }

    public void resetAllAccounts() {
        accountLock.lock();
        try {
            log.warn("Forcibly resetting all account states");

            // 모든 큐 비우기
            for (OperationType type : OperationType.values()) {
                availableAccounts.get(type).clear();
            }

            // 모든 계정 사용 가능 상태로 변경
            for (RelayerAccount account : accounts) {
                account.markAvailable();
            }

            // 계정 재할당
            assignAccountsToOperations();

            log.info("Account reset completed");
            logAccountStatus();
        } finally {
            accountLock.unlock();
        }
    }
}