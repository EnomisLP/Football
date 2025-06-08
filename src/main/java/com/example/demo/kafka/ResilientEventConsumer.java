package com.example.demo.kafka;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.example.demo.DTO.ArticlesNodeDTO;
import com.example.demo.DTO.CoachesNodeDTO;
import com.example.demo.DTO.PlayersNodeDTO;
import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.DTO.UsersNodeDTO;
import com.example.demo.configurations.Neo4j.Neo4jHealthChecker;
import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.OutboxEvent;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.Neo4j.ArticlesNode;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.Neo4j.Articles_node_rep;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.repositories.Neo4j.Players_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.repositories.Neo4j.Users_node_rep;
import com.example.demo.services.Neo4j.Articles_node_service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResilientEventConsumer {

    private final Users_node_rep usersNodeRepository;
    private final Articles_node_rep articlesNodeRepository;
    private final Articles_node_service articlesNodeService;
    private final Players_node_rep playersNodeRepository;
    private final Players_repository playersRepository;
    private final ObjectMapper objectMapper;
    private final Neo4jHealthChecker neo4jHealthChecker;
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final Integer MAX_NUMBER_TEAM = 11;
    private final Teams_node_rep teamsNodeRepository;
    private final Coaches_node_rep coachesNodeRepository;

    public ResilientEventConsumer(
            Users_node_rep usersNodeRepository, 
            Articles_node_rep articlesNodeRepository,
            Articles_node_service articlesNodeService,
            ObjectMapper objectMapper,
            Neo4jHealthChecker neo4jHealthChecker,
            MongoTemplate mongoTemplate,
            KafkaTemplate<String, String> kafkaTemplate,
            Players_node_rep pm, Players_repository PM,
            Teams_node_rep tm, Coaches_node_rep cm) {
        this.usersNodeRepository = usersNodeRepository;
        this.articlesNodeRepository = articlesNodeRepository;
        this.articlesNodeService = articlesNodeService;
        this.objectMapper = objectMapper;
        this.neo4jHealthChecker = neo4jHealthChecker;
        this.mongoTemplate = mongoTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.playersNodeRepository = pm;
        this.playersRepository = PM;
        this.teamsNodeRepository = tm;
        this.coachesNodeRepository = cm;
    }

    @KafkaListener(topics = "application-events", groupId = "my-app-group")
    public void listen(ConsumerRecord<String, String> record) {
        String eventType = record.key();
        String payload = record.value();

        log.info("Processing event: {} with payload: {}", eventType, payload);

        try {
            // Prima verifica se Neo4j è disponibile
            if (neo4jHealthChecker.isNeo4jHealthy()) {
                // Neo4j disponibile - processa immediatamente
                processEventDirectly(eventType, payload);
            } else {
                // Neo4j down - salva per processing successivo
                saveEventForLaterProcessing(eventType, payload);
                log.warn("Neo4j is down, event {} saved for later processing", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing event {}: {}", eventType, e.getMessage());
            handleEventFailure(eventType, payload, e);
        }
    }

    private void processEventDirectly(String eventType, String payload) throws JsonProcessingException {
        switch (eventType) {
            case "Neo4jArticleCreated":
                handleNeo4jArticleCreated(payload);
                break;
            case "Neo4jArticleUpdated":
                handleNeo4jArticleUpdate(payload);
                break;
            case "Neo4jArticleDeleted":
                handleNeo4jArticleDelete(payload);
                break;
            case "Neo4jUserUpdated":
                handleNeo4jUpdateUser(payload);
                break;
            case "Neo4jUserCreated":
                handleUserCreation(payload);
                break;
            case "Neo4jUserDeleted":
                handleNeo4jDeleteUser(payload);
                break;
            case "Neo4jAddInMTeam":
                handleNeo4jAddInMTeam(payload);
                break;
            case "Neo4jAddInFTeam":
                handleNeo4jAddInFTeam(payload);
                break;
            case "Neo4jRemoveInMTeam":
                handleNeo4jRemoveInMTeam(payload);
                break;
            case "Neo4jRemoveInFTeam":
                handleNeo4jRemoveInFTeam(payload);
                break;
            case "Neo4jLikePlayer":
                handleNeo4jLikePlayer(payload);
                break;
             case "Neo4jUnlikePlayer":
                handleNeo4jUnlikePlayer(payload);
                break;
             case "Neo4jLikeCoach":
                handleNeo4jLikeCoach(payload);
                break;
             case "Neo4jUnlikeCoach":
                handleNeo4jUnlikeCoach(payload);
                break;
             case "Neo4jLikeTeam":
                handleNeo4jLikeTeam(payload);
                break;
            case "Neo4jUnlikeTeam":
                handleNeo4jUnlikeTeam(payload);
                break;
            case "Neo4jLikeArticle":
                handleNeo4jLikeArticle(payload);
                break;
            case "Neo4jUnlikeArticle":
                handleNeo4jUnlikeArticle(payload);
                break;
            case "Neo4jFollowUser":
                handleNeo4jFollow(payload);
                break;
            case "Neo4jUnfollowUser":
                handleNeo4jUnFollow(payload);
                break;
            default:
                log.warn("Unhandled event type: {}", eventType);
        }
    }

        @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
        private void handleNeo4jArticleCreated(String payload) throws JsonProcessingException {
            Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
            String articleId = (String) eventData.get("articleId");
            String username = (String) eventData.get("username");

            try {
                // Verifica che i prerequisiti esistano
                Optional<UsersNodeDTO> userOptional = usersNodeRepository.findByUserNameLight(username);
                if (userOptional.isEmpty()) {
                    log.error("User not found for article creation: {}", username);
                    return;
                }

                // Verifica se l'articolo già exists (idempotenza)
                Optional<ArticlesNodeDTO> existingArticle = articlesNodeRepository.findByMongoIdLight(articleId);
                if (existingArticle.isPresent()) {
                    log.info("Article {} already exists in Neo4j, skipping creation", articleId);
                    return;
                }

                // Crea l'articolo
                ArticlesNode article = new ArticlesNode();
                article.setMongoId(articleId);
                article.setTitle((String) eventData.get("title"));
                article.setAuthor(username);
                articlesNodeRepository.save(article);

                // Crea la relazione
                usersNodeRepository.createWroteRelationToArticle(username, articleId);
                
                log.info("Successfully created article {} and relation for user {}", articleId, username);

            } catch (Exception e) {
                log.error("Failed to create article {} for user {}: {}", articleId, username, e.getMessage());
                throw e; // Per trigger del retry
            }
        }
    
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional
    private void handleNeo4jArticleUpdate(String payload) throws JsonProcessingException {
        Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String articleId = (String) eventData.get("articleId");
        String title = (String) eventData.get("title");

        try {
            Optional<ArticlesNodeDTO> articleOptional = articlesNodeRepository.findByMongoIdLight(articleId);
            if (articleOptional.isPresent()) {
                articlesNodeRepository.updateArticleTitle(articleId, title);
               
                log.info("Successfully updated article {}", articleId);
            } else {
                log.warn("Article not found for update: {}", articleId);
            }
        } catch (Exception e) {
            log.error("Failed to update article {}: {}", articleId, e.getMessage());
            throw e;
        }
    }
    
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional
    public void handleNeo4jArticleDelete(String payload) throws JsonProcessingException {
        Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String articleId = (String) eventData.get("articleId");

        try {
            Optional<ArticlesNodeDTO> articleOptional = articlesNodeRepository.findByMongoIdLight(articleId);
            if (articleOptional.isPresent()) {
                articlesNodeRepository.deleteByMongoIdLight(articleId);    
                log.info("Successfully deleted article {}", articleId);
            } else {
                log.warn("Article not found for delete: {}", articleId);
            }
        } catch (Exception e) {
            log.error("Failed to delete article {}: {}", articleId, e.getMessage());
            throw e;
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional
    private void handleNeo4jUpdateUser(String payload) throws JsonProcessingException {
        Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String oldUsername = (String) eventData.get("oldUserName");
        String newUsername = (String) eventData.get("newUserName");

        try {
            Optional<UsersNodeDTO> userOptional = usersNodeRepository.findByUserNameLight(oldUsername);
            if (userOptional.isPresent()) {
                
                usersNodeRepository.updateUserNameByUserName(oldUsername, newUsername);
                log.info("Successfully updated user from {} to {}", oldUsername, newUsername);
            } else {
                log.warn("User not found for update: {}", oldUsername);
            }
        } catch (Exception e) {
            log.error("Failed to update user {}: {}", oldUsername, e.getMessage());
            throw e;
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))

    private void handleUserCreation(String payload) throws JsonProcessingException {
        Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String mongoId = (String) eventData.get("mongoId");

        try {
            // Verifica idempotenza
            Optional<UsersNodeDTO> existingUser = usersNodeRepository.findByUserNameLight(username);
            if (existingUser.isPresent()) {
                log.info("User {} already exists in Neo4j, skipping creation", username);
                return;
            }

            UsersNode user = new UsersNode();
            user.setUserName(username);
            user.setMongoId(mongoId);
            usersNodeRepository.save(user);
            log.info("Successfully created user {}", username);

        } catch (Exception e) {
            log.error("Failed to create user {}: {}", username, e.getMessage());
            throw e;
        }
    }
    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    private void handleNeo4jDeleteUser(String payload) throws JsonProcessingException {
        Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");

        try {
            Optional<UsersNodeDTO> userOptional = usersNodeRepository.findByUserNameLight(username);
            if (userOptional.isPresent()) {
                articlesNodeRepository.deleteUserArticles(username);
                usersNodeRepository.deleteUserByUserNameLight(username);
                log.info("Successfully deleted user {}", username);
            } else {
                log.warn("User not found for delete: {}", username);
            }
        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", username, e.getMessage());
            throw e;
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private String handleNeo4jAddInMTeam(String payload) throws JsonProcessingException{
        Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String playerMongoId = (String) eventData.get("playerMongoId");
        Integer fifaVersion = (Integer) eventData.get("fifaVersion");
        Optional<UsersNodeDTO> userNodeOpt = usersNodeRepository.findByUserNameLight(username);
        Optional<PlayersNodeDTO> playerNodeOpt = playersNodeRepository.findByMongoIdLight(playerMongoId);

        if (userNodeOpt.isEmpty() || playerNodeOpt.isEmpty()) {
            throw new IllegalArgumentException("User or Player not found.");
        }

        PlayersNodeDTO playerNode = playerNodeOpt.get();

        if (playerNode.getGender().equals("female")) {
            throw new IllegalArgumentException("Female players can't be added into Male Team");
        }

        Optional<Players> mongoPlayerOpt = playersRepository.findById(playerNode.getMongoId());
        if (mongoPlayerOpt.isEmpty() || mongoPlayerOpt.get().getFifaStats().isEmpty()) {
            throw new IllegalArgumentException("FIFA stats not found for Player id: " + playerMongoId);
        }

        Players mongoPlayer = mongoPlayerOpt.get();
        List<FifaStatsPlayer> fifaStats = mongoPlayer.getFifaStats();


        for (FifaStatsPlayer stat : fifaStats) {
            if (stat.getFifa_version().equals(fifaVersion)) {
                if(usersNodeRepository.countPlayersInMTeamByUsername(username) < MAX_NUMBER_TEAM){
                    usersNodeRepository.createHasInMTeamRelation(username, playerMongoId, fifaVersion);
                    return "Player with id " + playerMongoId + " added to User " + username + "'s M team with FIFA Version " + fifaVersion;
                }
                else {
                    throw new IllegalArgumentException("Team can have maximum "+MAX_NUMBER_TEAM+" players!");
                }
            }
        }

        throw new IllegalArgumentException("FIFA version " + fifaVersion + " not found for Player id: " + playerMongoId);
    }
    
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private String handleNeo4jAddInFTeam(String payload) throws JsonProcessingException{
    Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
    String username = (String) eventData.get("username");
    String playerMongoId = (String) eventData.get("playerMongoId");
    Integer fifaVersion = (Integer) eventData.get("fifaVersion");
    Optional<UsersNodeDTO> userNodeOpt = usersNodeRepository.findByUserNameLight(username);
    Optional<PlayersNodeDTO> playerNodeOpt = playersNodeRepository.findByMongoIdLight(playerMongoId);

    if (userNodeOpt.isEmpty() || playerNodeOpt.isEmpty()) {
        throw new IllegalArgumentException("User or Player not found.");
    }

    PlayersNodeDTO playerNode = playerNodeOpt.get();

    if (playerNode.getGender().equals("male")) {
        throw new IllegalArgumentException("Female players can't be added into Male Team");
    }

    Optional<Players> mongoPlayerOpt = playersRepository.findById(playerNode.getMongoId());
    if (mongoPlayerOpt.isEmpty() || mongoPlayerOpt.get().getFifaStats().isEmpty()) {
        throw new IllegalArgumentException("FIFA stats not found for Player id: " + playerMongoId);
    }

    Players mongoPlayer = mongoPlayerOpt.get();
    List<FifaStatsPlayer> fifaStats = mongoPlayer.getFifaStats();

    // Check if the relationship with the same player and FIFA version already exists
    
        for (FifaStatsPlayer stat : fifaStats) {
            if (stat.getFifa_version().equals(fifaVersion)) {
                if(usersNodeRepository.countPlayersInFTeamByUsername(username) < MAX_NUMBER_TEAM){
                    usersNodeRepository.createHasInFTeamRelation(username, playerMongoId, fifaVersion);
                    return "Player with id " + playerMongoId + " added to User " + username + "'s F team with FIFA Version " + fifaVersion;
                }
                else{
                    throw new IllegalArgumentException("Team can have maximum "+MAX_NUMBER_TEAM+" players!");
                }
            }
        }

        throw new IllegalArgumentException("FIFA version " + fifaVersion + " not found for Player id: " + playerMongoId);
    }
    
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jRemoveInMTeam(String payload) throws JsonProcessingException{
         Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String playerMongoId = (String) eventData.get("playerMongoId");
        Optional<UsersNodeDTO> optionalUserNode = usersNodeRepository.findByUserNameLight(username);
        Optional<PlayersNodeDTO> optionalPlayerNode = playersNodeRepository.findByMongoIdLight(playerMongoId);
        
        if (optionalUserNode.isPresent() && optionalPlayerNode.isPresent()) {
            PlayersNodeDTO existingPlayersNode = optionalPlayerNode.get();
            
            if(existingPlayersNode.getGender().equals("male")) {
                // Use removeIf to safely remove the players from the M team
                usersNodeRepository.deleteHasInMTeamRelation(username, playerMongoId);
            } else {
                throw new RuntimeException("Player with id: " + playerMongoId + " is a female");
            }
        } else {
            throw new RuntimeException("Player with id: " + playerMongoId + " not found in the M team of user: " + username);
        }

    }
    
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jRemoveInFTeam(String payload) throws JsonProcessingException{
        Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String playerMongoId = (String) eventData.get("playerMongoId");
        Optional<UsersNodeDTO> optionalUserNode = usersNodeRepository.findByUserNameLight(username);
        Optional<PlayersNodeDTO> optionalPlayerNode = playersNodeRepository.findByMongoIdLight(playerMongoId);
        if (optionalUserNode.isPresent() && optionalPlayerNode.isPresent()) {
            PlayersNodeDTO existingPlayersNode = optionalPlayerNode.get();
            if(existingPlayersNode.getGender().equals("female")){
                // Use removeIf to safely remove the players from the M team
               usersNodeRepository.deleteHasInFTeamRelation(username, playerMongoId);
                
                // Save the updated UsersNode
            }
            else{
                throw new RuntimeException("Player with id: " + playerMongoId + " is a female");
            }
        } else {
            throw new RuntimeException("Player with id: " + playerMongoId + " not found in the M team of user: " + username);
        }
    }

     @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jLikeArticle(String payload) throws JsonProcessingException{
       Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String articleId = (String) eventData.get("articleId");
        Optional<ArticlesNodeDTO> optional = articlesNodeRepository.findByMongoIdLight(articleId);
        if(optional.isPresent()){
            usersNodeRepository.createLikeRelationToArticle(username, articleId);
        }
        else{
            throw new RuntimeErrorException(null, "Article not found with id: " + articleId);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jUnlikeArticle(String payload) throws JsonProcessingException{
       Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String articleId = (String) eventData.get("articleId");
        Optional<ArticlesNodeDTO> optional = articlesNodeRepository.findByMongoIdLight(articleId);
        if(optional.isPresent()){
            usersNodeRepository.deleteLikeRelationToArticle(username, articleId);
        }
        else{
            throw new RuntimeErrorException(null, "Article not found with id: " + articleId);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jFollow(String payload) throws JsonProcessingException{
       Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String targetUsername = (String) eventData.get("targetUsername");
        Optional<UsersNodeDTO> optional = usersNodeRepository.findByUserNameLight(targetUsername);
        if(optional.isPresent() && !username.equals(targetUsername)){

            usersNodeRepository.createFollowRelation(username, targetUsername);
        }
        else{
            
            throw new RuntimeErrorException(null, "User not found with username : " + targetUsername);
        }
    }

     @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jUnFollow(String payload) throws JsonProcessingException{
       Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String targetUsername = (String) eventData.get("targetUsername");
        Optional<UsersNodeDTO> optional = usersNodeRepository.findByUserNameLight(targetUsername);
        if(optional.isPresent()){
             usersNodeRepository.removeFollowRelationship(username, targetUsername);
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username : " + targetUsername);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jLikeTeam(String payload) throws JsonProcessingException{
       Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String teamMongoId = (String) eventData.get("teamMongoId");
        Optional<TeamsNodeDTO> optional = teamsNodeRepository.findByMongoIdLight(teamMongoId);
        if(optional.isPresent()){
            usersNodeRepository.LikeToTeam(username, teamMongoId);
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + teamMongoId);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jUnlikeTeam(String payload) throws JsonProcessingException{
       Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String teamMongoId = (String) eventData.get("teamMongoId");
        Optional<TeamsNodeDTO> optional = teamsNodeRepository.findByMongoIdLight(teamMongoId);
        if(optional.isPresent()){
            usersNodeRepository.deleteLikeRelationToTeam(username, teamMongoId);
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + teamMongoId);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jLikeCoach(String payload) throws JsonProcessingException{
       Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String coachMongoId = (String) eventData.get("coachMongoId");
        Optional<CoachesNodeDTO> optional = coachesNodeRepository.findByMongoIdLight(coachMongoId);
        if(optional.isPresent()){
            usersNodeRepository.LikeToCoach(username, coachMongoId);
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + coachMongoId);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jUnlikeCoach(String payload) throws JsonProcessingException{
       Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String coachMongoId = (String) eventData.get("coachMongoId");
        Optional<CoachesNodeDTO> optional = coachesNodeRepository.findByMongoIdLight(coachMongoId);
        if(optional.isPresent()){
            usersNodeRepository.deleteLikeRelationToCoach(username, coachMongoId);
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + coachMongoId);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jLikePlayer(String payload) throws JsonProcessingException{
       Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String playerMongoId = (String) eventData.get("playerMongoId");
        Optional<PlayersNodeDTO> optional = playersNodeRepository.findByMongoIdLight(playerMongoId);
        if(optional.isPresent()){
            usersNodeRepository.LikeToPlayer(username, playerMongoId);
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + playerMongoId);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    
    private void handleNeo4jUnlikePlayer(String payload) throws JsonProcessingException{
       Map<String, Object> eventData = objectMapper.readValue(payload, new TypeReference<>() {});
        String username = (String) eventData.get("username");
        String playerMongoId = (String) eventData.get("playerMongoId");
        Optional<PlayersNodeDTO> optional = playersNodeRepository.findByMongoIdLight(playerMongoId);
        if(optional.isPresent()){
           usersNodeRepository.deleteLikeRelationToPlayer(username, playerMongoId);
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + playerMongoId);
        }
    }


    /**
     * Salva l'evento per processing successivo quando Neo4j torna disponibile
     */
    private void saveEventForLaterProcessing(String eventType, String payload) {
        try {
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setEventType(eventType);
            outboxEvent.setPayload(payload);
            outboxEvent.setCreatedAt(LocalDateTime.now());
            outboxEvent.setPublished(false);
            outboxEvent.setRetryCount(0);
            
            mongoTemplate.save(outboxEvent);
            log.info("Saved event {} for later processing", eventType);
            
        } catch (Exception e) {
            log.error("Failed to save event {} for later processing: {}", eventType, e.getMessage());
        }
    }

    /**
     * Gestisce fallimenti degli eventi (dopo tutti i retry)
     */
    private void handleEventFailure(String eventType, String payload, Exception error) {
        try {
            // Salva in dead letter collection
            OutboxEvent failedEvent = new OutboxEvent();
            failedEvent.setEventType(eventType + "_FAILED");
            failedEvent.setPayload(payload);
            failedEvent.setCreatedAt(LocalDateTime.now());
            failedEvent.setPublished(false);
            failedEvent.setRetryCount(999); // Marca come fallito definitivamente
            failedEvent.setFailed(true);
            failedEvent.setFailedAt(LocalDateTime.now());

            mongoTemplate.save(failedEvent, "failed_events");
            
            // Opzionalmente, invia a dead letter topic
            kafkaTemplate.send("dead-letter-topic", eventType, payload);
            
            log.error("Event {} moved to dead letter collection after failure: {}", eventType, error.getMessage());
            
        } catch (Exception e) {
            log.error("Failed to handle event failure for {}: {}", eventType, e.getMessage());
        }
    }

    /**
     * Metodo chiamato quando Neo4j torna disponibile per processare eventi in sospeso
     */
    public void processBacklogEvents() {
        if (!neo4jHealthChecker.isNeo4jHealthy()) {
            log.warn("Neo4j still not healthy, skipping backlog processing");
            return;
        }

        Query query = new Query(
            Criteria.where("processed").is(false)
                   .and("retryCount").lt(5)
                   .and("eventType").not().regex(".*_FAILED$")
        );
        
        mongoTemplate.find(query, OutboxEvent.class).forEach(event -> {
            try {
                processEventDirectly(event.getEventType(), event.getPayload());
                
                // Marca come processato
                Update update = new Update()
                    .set("processed", true)
                    .set("processedAt", LocalDateTime.now());
                    
                mongoTemplate.updateFirst(
                    Query.query(Criteria.where("id").is(event.get_id())), 
                    update, 
                    OutboxEvent.class
                );
                
                log.info("Successfully processed backlog event: {}", event.getEventType());
                
            } catch (Exception e) {
                // Incrementa retry count
                Update update = new Update().inc("retryCount", 1);
                mongoTemplate.updateFirst(
                    Query.query(Criteria.where("id").is(event.get_id())), 
                    update, 
                    OutboxEvent.class
                );
                
                log.error("Failed to process backlog event {}: {}", event.getEventType(), e.getMessage());
            }
        });
    }
}