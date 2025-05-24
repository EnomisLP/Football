package com.example.demo.configurations.Neo4j;

import org.springframework.context.ApplicationEvent;

public class Neo4jRecoveryEvent extends ApplicationEvent {
    private final long consecutiveFailures;
    
    public Neo4jRecoveryEvent() {
        this(0L);
    }
    
    public Neo4jRecoveryEvent(long consecutiveFailures) {
        super("Neo4jRecoveryEvent");
        this.consecutiveFailures = consecutiveFailures;
    }
    
    public long getConsecutiveFailures() {
        return consecutiveFailures;
    }
}