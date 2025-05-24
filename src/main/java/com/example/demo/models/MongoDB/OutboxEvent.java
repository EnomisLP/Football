package com.example.demo.models.MongoDB;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Document(collection = "Outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    
    @Id
    private String _id;
    
    @Indexed
    private String eventType;
    
    private String payload;
    
    @Indexed
    private boolean published = false;
    
    @Indexed
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime publishedAt;
    
    @Indexed
    private int retryCount = 0;
    
    private LocalDateTime lastRetryAt;
    
    @Indexed
    private boolean failed = false;
    
    private LocalDateTime failedAt;
    
    private String lastError;
    
    // Campi aggiuntivi per metadati
    private String aggregateId;
    
    private String correlationId;
    
    private String source = "application";
    
    /**
     * Constructor per creazione rapida di eventi
     */
    public OutboxEvent(String eventType, String payload) {
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Constructor con aggregate ID
     */
    public OutboxEvent(String eventType, String payload, String aggregateId) {
        this(eventType, payload);
        this.aggregateId = aggregateId;
    }
    
    /**
     * Verifica se l'evento può essere ritentato
     */
    public boolean canRetry(int maxRetryCount) {
        return !failed && !published && retryCount < maxRetryCount;
    }
    
    /**
     * Incrementa il retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }
    
    /**
     * Marca l'evento come pubblicato
     */
    public void markAsPublished() {
        this.published = true;
        this.publishedAt = LocalDateTime.now();
    }
    
    /**
     * Marca l'evento come fallito
     */
    public void markAsFailed(String errorMessage) {
        this.failed = true;
        this.failedAt = LocalDateTime.now();
        this.lastError = errorMessage;
    }
    
    /**
     * Reset per nuovo tentativo
     */
    public void resetForRetry() {
        this.failed = false;
        this.failedAt = null;
        this.lastError = null;
    }
    
    /**
     * Verifica se è un evento Neo4j
     */
    public boolean isNeo4jEvent() {
        return eventType != null && eventType.startsWith("Neo4j");
    }
    
    @Override
    public String toString() {
        return String.format("OutboxEvent{id='%s', eventType='%s', published=%s, retryCount=%d, failed=%s}", 
                           _id, eventType, published, retryCount, failed);
    }
}