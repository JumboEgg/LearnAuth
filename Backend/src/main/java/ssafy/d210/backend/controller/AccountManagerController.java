package ssafy.d210.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.blockchain.AccountManager;

@Slf4j
@RestController
@RequestMapping("/api/admin/blockchain")
@RequiredArgsConstructor
public class AccountManagerController {

    private final AccountManager accountManager;

    @GetMapping("/accounts/reset")
    public ResponseEntity<String> resetAccounts() {
        log.info("Admin requested account reset");
        accountManager.resetAllAccounts();
        return ResponseEntity.ok("All blockchain accounts have been reset and reassigned");
    }

    @GetMapping("/accounts/status")
    public ResponseEntity<String> getAccountStatus() {
        log.info("Admin requested account status");
        accountManager.logAccountStatus();
        return ResponseEntity.ok("Account status has been logged to the application logs");
    }
}