package com.example.demo.repositories.MongoDB;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.models.MongoDB.OutboxEvent;

@Repository
public interface OutboxEvent_repository extends MongoRepository<OutboxEvent, String> {
    
    /**
     * Trova eventi non pubblicati (metodo originale)
     */
    List<OutboxEvent> findByPublishedFalse();
    
    /**
     * Trova eventi non pubblicati con retry count minore del limite, con paginazione
     */
    List<OutboxEvent> findByPublishedFalseAndRetryCountLessThan(int maxRetryCount, Pageable pageable);
    
    /**
     * Trova eventi falliti con retry count minore del limite
     */
    List<OutboxEvent> findByFailedTrueAndRetryCountLessThan(int maxRetryCount, Pageable pageable);
    
    /**
     * Conta eventi non pubblicati
     */
    long countByPublishedFalse();
    
    /**
     * Conta eventi falliti
     */
    long countByFailedTrue();
    
    
    /**
     * Trova eventi per tipo con filtro di stato
     */
    List<OutboxEvent> findByEventTypeAndPublishedFalse(String eventType);
    
    /**
     * Query custom per eventi Neo4j non processati quando Neo4j è down
     */
    @Query("{ 'eventType': { $regex: '^Neo4j' }, 'published': false, 'retryCount': { $lt: ?0 } }")
    List<OutboxEvent> findNeo4jEventsNotPublished(int maxRetryCount, Pageable pageable);
    
    /**
     * Trova eventi creati in un range temporale
     */
    List<OutboxEvent> findByCreatedAtBetweenAndPublishedFalse(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    /**
     * Conta eventi per tipo
     */
    long countByEventType(String eventType);
    
    /**
     * Trova eventi che hanno fallito più volte
     */
    @Query("{ 'retryCount': { $gte: ?0 }, 'failed': false }")
    List<OutboxEvent> findEventsWithHighRetryCount(int minRetryCount);

    int deleteByPublishedTrue();
}