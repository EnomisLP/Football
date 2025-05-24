package com.example.demo.configurations.Neo4j;

import java.time.Instant;

public class HealthStatus {
        private final boolean healthy;
        private final long consecutiveFailures;
        private final Instant lastFailureTime;
        private final boolean usingNoOpDriver;
        
        public HealthStatus(boolean healthy, long consecutiveFailures, 
                           Instant lastFailureTime, boolean usingNoOpDriver) {
            this.healthy = healthy;
            this.consecutiveFailures = consecutiveFailures;
            this.lastFailureTime = lastFailureTime;
            this.usingNoOpDriver = usingNoOpDriver;
        }
        
        public boolean isHealthy() { return healthy; }
        public long getConsecutiveFailures() { return consecutiveFailures; }
        public Instant getLastFailureTime() { return lastFailureTime; }
        public boolean isUsingNoOpDriver() { return usingNoOpDriver; }
        
        @Override
        public String toString() {
            return String.format("HealthStatus{healthy=%s, failures=%d, usingNoOp=%s}", 
                healthy, consecutiveFailures, usingNoOpDriver);
        }
    }
