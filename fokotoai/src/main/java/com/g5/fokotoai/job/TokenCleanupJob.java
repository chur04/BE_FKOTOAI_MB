package com.g5.fokotoai.job;
import com.g5.fokotoai.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupJob {
    private final PasswordResetTokenRepository tokenRepository;


    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void cleanExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(Instant.now());
        log.info("Cleaned up expired password reset tokens at {}", Instant.now());
    }
}
