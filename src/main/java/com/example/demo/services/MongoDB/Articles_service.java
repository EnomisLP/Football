package com.example.demo.services.MongoDB;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.management.RuntimeErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.repositories.MongoDB.OutboxEventRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.OutboxEvent;


import com.example.demo.models.MongoDB.Articles;

import com.example.demo.repositories.MongoDB.Articles_repository;




@Service
public class Articles_service {
    
    private final Articles_repository Ar;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public Articles_service(Articles_repository ar, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.Ar = ar;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    //READ
    public Articles getArticle(String id){
        
        Optional<Articles> article = Ar.findById(id);
        if(article.isPresent()){
            return article.get();
        }
        else{
            return null;
        }
    }
    public Page<Articles> getAllArticles(PageRequest pageable) {
        return Ar.findAll(pageable);
        
    }
    
    
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> deleteArticle(String username,String articleId) throws JsonProcessingException{
        
        Optional<Articles> optionalArticle = Ar.findById(articleId);
        if(optionalArticle.isPresent()){
            Articles existingArticle = optionalArticle.get();
            Ar.deleteById(articleId);
            //prepare Outbox event to delete ArticleNode in Neo4j
            Map<String, Object> neo4jPayload = new HashMap<>();
            neo4jPayload.put("articleId", existingArticle.get_id());
            neo4jPayload.put("admin", username);
            OutboxEvent neo4jArticleDeleteEvent = new OutboxEvent();
            neo4jArticleDeleteEvent.setEventType("Neo4jArticleDeleted");
            neo4jArticleDeleteEvent.setAggregateId(articleId);
            neo4jArticleDeleteEvent.setPayload(objectMapper.writeValueAsString(neo4jPayload));
            neo4jArticleDeleteEvent.setPublished(false);
            neo4jArticleDeleteEvent.setCreatedAt(LocalDateTime.now());
            outboxEventRepository.save(neo4jArticleDeleteEvent);
            return CompletableFuture.completedFuture("Article deleted correctly!");
        }
        else{
            throw new RuntimeErrorException(null, "Article not present with id:" + articleId);
        }
    }
    
}
