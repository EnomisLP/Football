package com.example.demo.configurations.Neo4j;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Neo4jHealthChecker {

    @Autowired
    private Neo4jDriverManager driverManager;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    private volatile boolean neo4jHealthy;
    private volatile boolean wasHealthy;
    private final AtomicLong lastCheckTime = new AtomicLong();
    private final AtomicReference<Instant> lastFailureTime = new AtomicReference<>();
    private final AtomicLong consecutiveFailures = new AtomicLong(0);
    
    // Configuration constants
    private static final long HEALTH_CHECK_INTERVAL_MS = 10_000; // 10 seconds
    private static final long SCHEDULED_CHECK_INTERVAL_MS = 30_000; // 30 seconds
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    public Neo4jHealthChecker() {
        // Initialize based on whether we start with a real connection
        this.neo4jHealthy = false; // Will be set correctly on first health check
        this.wasHealthy = false;
    }
    
    /**
     * Verifica se Neo4j è attualmente healthy
     */
    public boolean isNeo4jHealthy() {
        long now = System.currentTimeMillis();
        if (now - lastCheckTime.get() > HEALTH_CHECK_INTERVAL_MS) {
            checkNeo4jHealth();
            lastCheckTime.set(now);
        }
        return neo4jHealthy;
    }
    
    /**
     * Health check schedulato ogni 30 secondi
     */
    @Scheduled(fixedDelay = SCHEDULED_CHECK_INTERVAL_MS)
    public void scheduledHealthCheck() {
        log.debug("Performing scheduled Neo4j health check");
        checkNeo4jHealth();
    }
    
    /**
     * Esegue il controllo effettivo di Neo4j
     */
    private void checkNeo4jHealth() {
        boolean isCurrentlyHealthy = performHealthCheck();
        
        synchronized (this) {
            updateHealthStatus(isCurrentlyHealthy);
        }
    }
    
    /**
     * Performs the actual health check with reconnection attempts
     */
    private boolean performHealthCheck() {
        // If we don't have a real connection, try to reconnect
        if (!driverManager.hasRealConnection()) {
            if (driverManager.attemptReconnection()) {
                // Successfully reconnected, now test the connection
                return testConnection();
            } else {
                recordFailure(new ServiceUnavailableException("Neo4j still unavailable"));
                return false;
            }
        }
        
        // We have a real connection, test it
        return testConnection();
    }
    
    /**
     * Tests the current connection with retry logic
     */
    private boolean testConnection() {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return executeHealthQuery();
            } catch (ServiceUnavailableException | SessionExpiredException e) {
                log.warn("Neo4j health check failed (attempt {}/{}): {}", 
                    attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    recordFailure(e);
                    // Switch to NoOpDriver on final failure
                    driverManager.switchToNoOpDriver();
                    return false;
                }
                
                // Brief pause between retries
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            } catch (Exception e) {
                log.error("Unexpected error during Neo4j health check: {}", e.getMessage(), e);
                recordFailure(e);
                driverManager.switchToNoOpDriver();
                return false;
            }
        }
        return false;
    }
    
    /**
     * Executes the actual health query
     */
    private boolean executeHealthQuery() {
        Driver driver = driverManager.getDriver();
        try (Session session = driver.session()) {
            // Simple connectivity test
            session.run("RETURN 1 as healthCheck").consume();
            resetFailureCounter();
            return true;
        }
    }
    
    /**
     * Updates health status and triggers events if needed
     */
    private void updateHealthStatus(boolean isCurrentlyHealthy) {
        if (isCurrentlyHealthy && !neo4jHealthy) {
            // Neo4j recovered
            neo4jHealthy = true;
            onNeo4jRecovered();
        } else if (!isCurrentlyHealthy && neo4jHealthy) {
            // Neo4j went down
            neo4jHealthy = false;
            onNeo4jWentDown();
        }
        
        wasHealthy = neo4jHealthy;
    }
    
    /**
     * Records failure information
     */
    private void recordFailure(Exception e) {
        lastFailureTime.set(Instant.now());
        long failures = consecutiveFailures.incrementAndGet();
        log.debug("Recorded Neo4j failure #{}: {}", failures, e.getMessage());
    }
    
    /**
     * Resets failure counter on successful connection
     */
    private void resetFailureCounter() {
        if (consecutiveFailures.get() > 0) {
            long previousFailures = consecutiveFailures.getAndSet(0);
            log.debug("Neo4j connection restored after {} consecutive failures", previousFailures);
        }
    }
    
    /**
     * Chiamato quando Neo4j torna online
     */
    private void onNeo4jRecovered() {
        long failures = consecutiveFailures.get();
        log.info("🎉 Neo4j recovered! Connection restored after {} consecutive failures.", failures);
        
        // Pubblica evento di recovery
        try {
            eventPublisher.publishEvent(new Neo4jRecoveryEvent(failures));
        } catch (Exception e) {
            log.error("Failed to publish Neo4j recovery event: {}", e.getMessage());
        }
    }
    
    /**
     * Chiamato quando Neo4j va down
     */
    private void onNeo4jWentDown() {
        log.warn("⚠️ Neo4j connection lost. Switching to eventual consistency mode.");
    }
    
    /**
     * Forza un health check immediato
     */
    public void forceHealthCheck() {
        log.info("Forcing immediate Neo4j health check");
        checkNeo4jHealth();
    }
    
    /**
     * Returns current health status information
     */
    public HealthStatus getHealthStatus() {
        return new HealthStatus(
            neo4jHealthy,
            consecutiveFailures.get(),
            lastFailureTime.get(),
            !driverManager.hasRealConnection()
        );
    }
}