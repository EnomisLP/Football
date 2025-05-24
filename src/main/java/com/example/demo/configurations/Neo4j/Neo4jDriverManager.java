package com.example.demo.configurations.Neo4j;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages Neo4j driver lifecycle with dynamic reconnection capability
 */
@Component
@Slf4j
public class Neo4jDriverManager {
    
    private final String uri;
    private final String username;
    private final String password;
    private final Config driverConfig;
    
    private volatile Driver currentDriver;
    private volatile boolean isRealDriver = false;
    private final ReadWriteLock driverLock = new ReentrantReadWriteLock();
    
    public Neo4jDriverManager(@Value("${spring.neo4j.uri}") String uri,
                             @Value("${spring.neo4j.authentication.username}") String username,
                             @Value("${spring.neo4j.authentication.password}") String password) {
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.driverConfig = Config.builder()
                .withMaxConnectionLifetime(30, java.util.concurrent.TimeUnit.MINUTES)
                .withMaxConnectionPoolSize(50)
                .withConnectionAcquisitionTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        
        // Initialize with either real driver or NoOp
        initializeDriver();
    }
    
    private void initializeDriver() {
        try {
            Driver realDriver = GraphDatabase.driver(uri, AuthTokens.basic(username, password), driverConfig);
            realDriver.verifyConnectivity();
            
            driverLock.writeLock().lock();
            try {
                this.currentDriver = realDriver;
                this.isRealDriver = true;
            } finally {
                driverLock.writeLock().unlock();
            }
            
            log.info("✅ Connected to Neo4j successfully at startup");
        } catch (Exception e) {
            driverLock.writeLock().lock();
            try {
                this.currentDriver = new NoOpDriver();
                this.isRealDriver = false;
            } finally {
                driverLock.writeLock().unlock();
            }
            
            log.warn("⚠️ Neo4j is down at startup, using NoOpDriver: {}", e.getMessage());
        }
    }
    
    /**
     * Gets the current driver (thread-safe)
     */
    public Driver getDriver() {
        driverLock.readLock().lock();
        try {
            return currentDriver;
        } finally {
            driverLock.readLock().unlock();
        }
    }
    
    /**
     * Checks if we have a real Neo4j connection
     */
    public boolean hasRealConnection() {
        driverLock.readLock().lock();
        try {
            return isRealDriver;
        } finally {
            driverLock.readLock().unlock();
        }
    }
    
    /**
     * Attempts to establish a real connection to Neo4j
     * Returns true if successful, false otherwise
     */
    public boolean attemptReconnection() {
        if (isRealDriver) {
            return true; // Already connected
        }
        
        try {
            Driver newRealDriver = GraphDatabase.driver(uri, AuthTokens.basic(username, password), driverConfig);
            newRealDriver.verifyConnectivity();
            
            // Successfully connected, replace the NoOpDriver
            driverLock.writeLock().lock();
            try {
                Driver oldDriver = this.currentDriver;
                this.currentDriver = newRealDriver;
                this.isRealDriver = true;
                
                // Close old NoOpDriver (it's a no-op anyway)
                if (oldDriver != null) {
                    try {
                        oldDriver.close();
                    } catch (Exception e) {
                        log.debug("Error closing old driver: {}", e.getMessage());
                    }
                }
            } finally {
                driverLock.writeLock().unlock();
            }
            
            log.info("🎉 Successfully reconnected to Neo4j!");
            return true;
            
        } catch (Exception e) {
            log.debug("Neo4j reconnection attempt failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Switches back to NoOpDriver when connection is lost
     */
    public void switchToNoOpDriver() {
        if (!isRealDriver) {
            return; // Already using NoOpDriver
        }
        
        driverLock.writeLock().lock();
        try {
            Driver oldDriver = this.currentDriver;
            this.currentDriver = new NoOpDriver();
            this.isRealDriver = false;
            
            // Close the real driver
            if (oldDriver != null) {
                try {
                    oldDriver.close();
                } catch (Exception e) {
                    log.warn("Error closing real driver: {}", e.getMessage());
                }
            }
        } finally {
            driverLock.writeLock().unlock();
        }
        
        log.warn("⚠️ Switched to NoOpDriver due to connection loss");
    }
    
    @PreDestroy
    public void cleanup() {
        driverLock.writeLock().lock();
        try {
            if (currentDriver != null) {
                currentDriver.close();
            }
        } finally {
            driverLock.writeLock().unlock();
        }
    }
}