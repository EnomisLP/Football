package com.example.demo.configurations.Neo4j;

import lombok.extern.slf4j.Slf4j;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.BaseSession;
import org.neo4j.driver.BookmarkManager;
import org.neo4j.driver.Driver;
import org.neo4j.driver.ExecutableQuery;
import org.neo4j.driver.Metrics;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@Slf4j
@EnableNeo4jRepositories(basePackages = "com.example.demo.repositories.Neo4j")
public class Neo4jConfig {

    @Autowired
    private Neo4jDriverManager driverManager;

    @Bean
    public Driver neo4jDriver() {
        // Return a proxy that always delegates to the current driver
        return new DynamicDriverProxy(driverManager);
    }

    /**
     * Proxy that delegates to the current driver from DriverManager
     */
    private static class DynamicDriverProxy implements Driver {
        private final Neo4jDriverManager driverManager;

        public DynamicDriverProxy(Neo4jDriverManager driverManager) {
            this.driverManager = driverManager;
        }

        @Override
        public Session session() {
            return driverManager.getDriver().session();
        }

        @Override
        public Session session(SessionConfig config) {
            return driverManager.getDriver().session(config);
        }

        @Override
        public void verifyConnectivity() {
            driverManager.getDriver().verifyConnectivity();
        }

        @Override
        public void close() {
            // Don't close here - managed by DriverManager
        }

        @Override
        public boolean isEncrypted() {
            return driverManager.getDriver().isEncrypted();
        }

        @Override
        public ExecutableQuery executableQuery(String query) {
            return driverManager.getDriver().executableQuery(query);
        }

        @Override
        public BookmarkManager executableQueryBookmarkManager() {
            return driverManager.getDriver().executableQueryBookmarkManager();
        }

        @Override
        public <T extends BaseSession> T session(Class<T> sessionClass, SessionConfig sessionConfig, AuthToken sessionAuthToken) {
            return driverManager.getDriver().session(sessionClass, sessionConfig, sessionAuthToken);
        }

        @Override
        public java.util.concurrent.CompletionStage<Void> closeAsync() {
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }

        @Override
        public Metrics metrics() {
            return driverManager.getDriver().metrics();
        }

        @Override
        public boolean isMetricsEnabled() {
            return driverManager.getDriver().isMetricsEnabled();
        }

        @Override
        public org.neo4j.driver.types.TypeSystem defaultTypeSystem() {
            return driverManager.getDriver().defaultTypeSystem();
        }

        @Override
        public java.util.concurrent.CompletionStage<Void> verifyConnectivityAsync() {
            return driverManager.getDriver().verifyConnectivityAsync();
        }

        @Override
        public boolean verifyAuthentication(AuthToken authToken) {
            return driverManager.getDriver().verifyAuthentication(authToken);
        }

        @Override
        public boolean supportsSessionAuth() {
            return driverManager.getDriver().supportsSessionAuth();
        }

        @Override
        public boolean supportsMultiDb() {
            return driverManager.getDriver().supportsMultiDb();
        }

        @Override
        public java.util.concurrent.CompletionStage<Boolean> supportsMultiDbAsync() {
            return driverManager.getDriver().supportsMultiDbAsync();
        }
    }
}