package com.example.demo.configurations.Neo4j;


import org.neo4j.driver.*;


 class NoOpDriver implements Driver {
        @Override
        public Session session() {
            throw new org.neo4j.driver.exceptions.ServiceUnavailableException("Neo4j is unavailable");
        }

        @Override
        public Session session(SessionConfig config) {
            throw new org.neo4j.driver.exceptions.ServiceUnavailableException("Neo4j is unavailable");
        }

        @Override
        public void verifyConnectivity() {
            throw new org.neo4j.driver.exceptions.ServiceUnavailableException("Neo4j is unavailable");
        }

        @Override
        public void close() {
            // No-op
        }

        @Override
        public boolean isEncrypted() {
            return false;
        }

        @Override
        public ExecutableQuery executableQuery(String query) {
            throw new org.neo4j.driver.exceptions.ServiceUnavailableException("Neo4j is unavailable");
        }

        @Override
        public BookmarkManager executableQueryBookmarkManager() {
            throw new UnsupportedOperationException("Neo4j is unavailable");
        }

        @Override
        public <T extends BaseSession> T session(Class<T> sessionClass, SessionConfig sessionConfig, AuthToken sessionAuthToken) {
            throw new org.neo4j.driver.exceptions.ServiceUnavailableException("Neo4j is unavailable");
        }

        @Override
        public java.util.concurrent.CompletionStage<Void> closeAsync() {
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }

        @Override
        public Metrics metrics() {
            throw new UnsupportedOperationException("Neo4j is unavailable");
        }

        @Override
        public boolean isMetricsEnabled() {
            return false;
        }

        @Override
        public org.neo4j.driver.types.TypeSystem defaultTypeSystem() {
            throw new UnsupportedOperationException("Neo4j is unavailable");
        }

        @Override
        public java.util.concurrent.CompletionStage<Void> verifyConnectivityAsync() {
            return java.util.concurrent.CompletableFuture.failedFuture(
                new org.neo4j.driver.exceptions.ServiceUnavailableException("Neo4j is unavailable")
            );
        }

        @Override
        public boolean verifyAuthentication(AuthToken authToken) {
            return false;
        }

        @Override
        public boolean supportsSessionAuth() {
            return false;
        }

        @Override
        public boolean supportsMultiDb() {
            return false;
        }

        @Override
        public java.util.concurrent.CompletionStage<Boolean> supportsMultiDbAsync() {
            return java.util.concurrent.CompletableFuture.completedFuture(false);
        }
    }

