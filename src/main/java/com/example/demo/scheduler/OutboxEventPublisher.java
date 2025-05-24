package com.example.demo.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import com.example.demo.configurations.Neo4j.Neo4jHealthChecker;
import com.example.demo.configurations.Neo4j.Neo4jRecoveryEvent;
import com.example.demo.models.MongoDB.OutboxEvent;
import com.example.demo.repositories.MongoDB.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Neo4jHealthChecker neo4jHealthChecker;
    
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 5;

    public OutboxEventPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            Neo4jHealthChecker neo4jHealthChecker) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.neo4jHealthChecker = neo4jHealthChecker;
    }

    /**
     * Gestisce il recovery di Neo4j processando eventi non pubblicati
     */
    @EventListener
    @Async
    public void handleNeo4jRecovery(Neo4jRecoveryEvent event) {
        log.info("Neo4j recovered, processing unpublished events...");
        processUnprocessedEvents();
    }

    /**
     * Scheduler principale che pubblica eventi outbox ogni 5 secondi
     */
    @Scheduled(fixedDelay = 5000)
    public void publishOutboxEvents() {
        try {
            processUnprocessedEvents();
        } catch (Exception e) {
            log.error("Error in scheduled outbox processing: {}", e.getMessage());
        }
    }

    /**
     * Processa eventi non pubblicati in batch per migliori performance
     */
    private void processUnprocessedEvents() {
        boolean hasMoreEvents = true;
        int processedTotal = 0;

        while (hasMoreEvents) {
            // Recupera batch di eventi non pubblicati
            List<OutboxEvent> events = outboxEventRepository.findByPublishedFalseAndRetryCountLessThan(
                MAX_RETRY_COUNT, 
                PageRequest.of(0, BATCH_SIZE, Sort.by("createdAt").ascending())
            );

            if (events.isEmpty()) {
                hasMoreEvents = false;
                continue;
            }

            // Processa ogni evento nel batch
            for (OutboxEvent event : events) {
                processOutboxEvent(event);
                processedTotal++;
            }

            // Se il batch è pieno, potrebbe esserci ancora eventi
            hasMoreEvents = events.size() == BATCH_SIZE;
        }

        if (processedTotal > 0) {
            log.info("Processed {} outbox events", processedTotal);
        }
    }

    /**
     * Processa un singolo evento outbox
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOutboxEvent(OutboxEvent event) {
        try {
            // Per eventi Neo4j, controlla se Neo4j è disponibile
            if (isNeo4jEvent(event.getEventType()) && !neo4jHealthChecker.isNeo4jHealthy()) {
                log.debug("Neo4j is down, skipping event: {}", event.getEventType());
                return;
            }

            // Pubblica su Kafka
            publishEventToKafka(event)
                .thenAccept(result -> markEventAsPublished(event))
                .exceptionally(failure -> handleEventFailure(event, failure));

        } catch (Exception e) {
            log.error("Exception processing outbox event {}: {}", event.get_id(), e.getMessage());
            handleEventFailure(event, e);
        }
    }

    /**
     * Pubblica evento su Kafka in modo asincrono
     */
    private CompletableFuture<Void> publishEventToKafka(OutboxEvent event) {
        String topic = determineTopic(event.getEventType());
        
        return kafkaTemplate.send(topic, event.getEventType(), event.getPayload())
            .thenAccept(result -> {
                log.debug("Successfully published event {} to topic {}", 
                         event.get_id(), topic);
            });
    }

    /**
     * Determina il topic Kafka basato sul tipo di evento
     */
    private String determineTopic(String eventType) {
        if (isNeo4jEvent(eventType)) {
            return "application-events";
        }
        // Aggiungi altri topic se necessario
        return "application-events";
    }

    /**
     * Verifica se è un evento destinato a Neo4j
     */
    private boolean isNeo4jEvent(String eventType) {
        return eventType != null && eventType.startsWith("Neo4j");
    }

    /**
     * Marca evento come pubblicato con successo
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markEventAsPublished(OutboxEvent event) {
        try {
            // Ricarica l'evento dal database per evitare problemi di concorrenza
            OutboxEvent currentEvent = outboxEventRepository.findById(event.get_id())
                .orElse(null);
                
            if (currentEvent != null && !currentEvent.isPublished()) {
                currentEvent.setPublished(true);
                currentEvent.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(currentEvent);
                
                log.debug("Marked event {} as published", event.get_id());
            }
        } catch (Exception e) {
            log.error("Failed to mark event {} as published: {}", 
                     event.get_id(), e.getMessage());
        }
    }

    /**
     * Gestisce fallimenti nella pubblicazione di eventi
     */
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
public Void handleEventFailure(OutboxEvent event, Throwable failure) {
    try {
        // Use the ID to ensure we're working with a fresh entity
        String eventId = event.get_id();
        
        // Load fresh entity within this transaction
        OutboxEvent currentEvent = outboxEventRepository.findById(eventId)
            .orElse(null);
            
        if (currentEvent == null) {
            log.warn("Event {} not found in database, cannot update failure count", eventId);
            return null;
        }
        
        if (currentEvent.isPublished()) {
            log.debug("Event {} already published, skipping failure handling", eventId);
            return null;
        }
        
        // Log current state before update
        log.debug("Event {} current retry count: {}, updating to: {}", 
                  eventId, currentEvent.getRetryCount(), currentEvent.getRetryCount() + 1);
        
        // Update failure information
        int newRetryCount = currentEvent.getRetryCount() + 1;
        currentEvent.setRetryCount(newRetryCount);
        currentEvent.setLastRetryAt(LocalDateTime.now());
        
        // Check if we've exceeded max retries
        if (newRetryCount >= MAX_RETRY_COUNT) {
            currentEvent.setFailed(true);
            currentEvent.setFailedAt(LocalDateTime.now());
            log.error("Event {} failed permanently after {} retries: {}", 
                      eventId, MAX_RETRY_COUNT, failure.getMessage());
        } else {
            log.warn("Event {} failed (retry {}/{}): {}", 
                     eventId, newRetryCount, MAX_RETRY_COUNT, failure.getMessage());
        }
        
        // Explicitly save and flush to ensure immediate persistence
        OutboxEvent savedEvent = outboxEventRepository.save(currentEvent);
        
        // Verify the save worked
        log.debug("Event {} saved with retry count: {}", eventId, savedEvent.getRetryCount());
        
    } catch (Exception e) {
        log.error("Failed to handle event failure for {}: {}", 
                  event.get_id(), e.getMessage(), e);
        // Re-throw to trigger transaction rollback if needed
        throw new RuntimeException("Failed to update event failure count", e);
    }
    return null;
}

    /**
     * Reprocessa eventi falliti (da chiamare manualmente o schedulato)
     */
    @Scheduled(fixedDelay = 300000) // Ogni 5 minuti
    public void reprocessFailedEvents() {
        List<OutboxEvent> failedEvents = outboxEventRepository.findByFailedTrueAndRetryCountLessThan(
            MAX_RETRY_COUNT, 
            PageRequest.of(0, 50)
        );

        if (!failedEvents.isEmpty()) {
            log.info("Reprocessing {} previously failed events", failedEvents.size());
            
            for (OutboxEvent event : failedEvents) {
                // Reset failed status per nuovo tentativo
                event.setFailed(false);
                outboxEventRepository.save(event);
                
                // Processa l'evento
                processOutboxEvent(event);
            }
        }
    }

    /**
     * Pulisce eventi vecchi già pubblicati (pulizia periodica)
     */
    @Scheduled(fixedDelay = 100000)
    public void cleanupOldEvents() {
       
        
        try {
            int deletedCount = outboxEventRepository.deleteByPublishedTrue();
            
            if (deletedCount > 0) {
                log.info("Cleaned up {} old published events", deletedCount);
            }
        } catch (Exception e) {
            log.error("Error during cleanup of old events: {}", e.getMessage());
        }
    }

    /**
     * Metrica per monitoraggio: conta eventi non processati
     */
    public long getUnprocessedEventCount() {
        return outboxEventRepository.countByPublishedFalse();
    }

    /**
     * Metrica per monitoraggio: conta eventi falliti
     */
    public long getFailedEventCount() {
        return outboxEventRepository.countByFailedTrue();
    }
}