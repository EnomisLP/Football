package com.example.demo.services.MongoDB;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.DTO.MongoUserDTO;
import com.example.demo.models.MongoDB.Articles;
import com.example.demo.models.MongoDB.OutboxEvent;
import com.example.demo.models.MongoDB.ROLES;
import com.example.demo.models.MongoDB.Users;
import com.example.demo.repositories.MongoDB.Articles_repository;
import com.example.demo.repositories.MongoDB.OutboxEvent_repository;
import com.example.demo.repositories.MongoDB.Users_repository;
import com.example.demo.repositories.Neo4j.Articles_node_rep;
import com.example.demo.repositories.Neo4j.Users_node_rep;
import com.example.demo.requets.createArticleRequest;
import com.example.demo.requets.createUserRequest;
import com.example.demo.services.Neo4j.Users_node_service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;


@Service
public class Users_service {

    private final Users_repository Ur;
    private final Users_node_rep UNr;
    private final Users_node_service UNs;
    private final PasswordEncoder passwordEncoder;
    private final Articles_repository Ar;
    private final Articles_node_rep AR;
    private final OutboxEvent_repository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public Users_service(Users_repository ur, Users_node_rep UNR, PasswordEncoder pe,
    Articles_repository ar, Users_node_service uns, Articles_node_rep articleNode, OutboxEvent_repository outboxEventRepository, ObjectMapper objectMapper) {
        this.Ur = ur;
        this.UNr = UNR;
        this.passwordEncoder=pe;
        this.Ar = ar;
        this.UNs = uns;
        this.AR = articleNode;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }
    
    public Page<Users> getAllUsers(PageRequest page){
        return Ur.findAll(page);
    }
    
    public Users getUserByUsername(String username) {
        Optional<Users> user = Ur.findByUsername(username);
        if(user.isPresent()){
            return user.get();
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username: " + username);
        }
    }
    public MongoUserDTO getUser(String id){
        Optional<Users> optional = Ur.findById(id);
        if(optional.isPresent()){
            Users user = optional.get();
            MongoUserDTO DTO = new MongoUserDTO();
            DTO.setUsername(user.getUsername());
            DTO.setEmail(user.getE_mail());
            DTO.setNationality_name(user.getNationality_name());
            return DTO;
        }
        else{
            throw new RuntimeErrorException(null, "User not found with id: "+id);
        }
    }

    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Users> createUser(createUserRequest request) throws JsonProcessingException {
        Optional<Users> optionalUser = Ur.findByUsername(request.getUsername());
        if(!optionalUser.isPresent()){
            Users userNew = new Users();
            LocalDateTime currentDate = LocalDateTime.now();
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String signUpDate=currentDate.format(timeFormat);
            
            userNew.setUsername(request.getUsername());
            userNew.setRoles(request.getRoles());
            userNew.setNationality_name(request.getNationality_name());
            userNew.setE_mail(request.getE_mail());
            userNew.setSignup_date(signUpDate.toString());
            // Hash the password using BCrypt
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            userNew.setPassword(hashedPassword);
            Ur.save(userNew);

            // Prepare Outbox event to create UsersNode in Neo4j
            Map<String, Object> neo4jPayload = new HashMap<>();
            neo4jPayload.put("username", userNew.getUsername());
            neo4jPayload.put("mongoId", userNew.get_id());

            OutboxEvent neo4jUserCreatedEvent = new OutboxEvent();
            neo4jUserCreatedEvent.setEventType("Neo4jUserCreated");
            neo4jUserCreatedEvent.setAggregateId(userNew.get_id());
            neo4jUserCreatedEvent.setPayload(objectMapper.writeValueAsString(neo4jPayload));
            neo4jUserCreatedEvent.setPublished(false);
            neo4jUserCreatedEvent.setCreatedAt(LocalDateTime.now());
            outboxEventRepository.save(neo4jUserCreatedEvent);
            return CompletableFuture.completedFuture(userNew);
        }
        else{
            throw new RuntimeException("Username already taken");
        }
        
    }

    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Users> updateUserName(String oldUserName, String newUserName) throws JsonProcessingException {
        // Check if the user exists in Neo4j
       Optional<Users> optionalUser = Ur.findByUsername(oldUserName);
        if (optionalUser.isPresent()) {
            Users existingUser = optionalUser.get();
                existingUser.setUsername(newUserName);
                Ur.save(existingUser);
                 // Prepare Outbox event to update UsersNode in Neo4j
                Map<String, Object> neo4jPayload = new HashMap<>();
                neo4jPayload.put("oldUserName", oldUserName);
                neo4jPayload.put("newUserName", newUserName);

                OutboxEvent neo4jUserUpdateEvent = new OutboxEvent();
                neo4jUserUpdateEvent.setEventType("Neo4jUserUpdated");
                neo4jUserUpdateEvent.setAggregateId(existingUser.get_id());
                neo4jUserUpdateEvent.setPayload(objectMapper.writeValueAsString(neo4jPayload));
                neo4jUserUpdateEvent.setPublished(false);
                neo4jUserUpdateEvent.setCreatedAt(LocalDateTime.now());
                outboxEventRepository.save(neo4jUserUpdateEvent);
                return CompletableFuture.completedFuture(existingUser);

            }
            else{
                throw new RuntimeException("User with username: " + oldUserName + " not correctly mapped in Neo4j");
            }
    }

    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Void> deleteUser(String username) throws JsonProcessingException {
        Optional<Users> user = Ur.findByUsername(username);
        List<Articles> userArticles = Ar.findByAuthor(username);
        if(user.isPresent()){
            Users existingUser = user.get();
            // Prepare Outbox event to delete UsersNode in Neo4j
            Map<String, Object> neo4jPayload = new HashMap<>();
            neo4jPayload.put("username", username);
            OutboxEvent neo4jUserDeleteEvent = new OutboxEvent();
            neo4jUserDeleteEvent.setEventType("Neo4jUserDeleted");
            neo4jUserDeleteEvent.setAggregateId(user.get().get_id());
            neo4jUserDeleteEvent.setPayload(objectMapper.writeValueAsString(neo4jPayload));
            neo4jUserDeleteEvent.setPublished(false);
            neo4jUserDeleteEvent.setCreatedAt(LocalDateTime.now());
            outboxEventRepository.save(neo4jUserDeleteEvent);
            Ur.deleteById(existingUser.get_id());
            if(!userArticles.isEmpty()){
                for(Articles article : userArticles){
                    Ar.delete(article);
                }
            }
            return CompletableFuture.completedFuture(null);
        }
        
        else{
            throw new RuntimeErrorException(null, "User not found with userName: " + username);
        }
       
    }
    //ARTICLES OPERATIONS
@Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public CompletableFuture<Articles> createArticle(String username, createArticleRequest request) throws JsonProcessingException {
    // 1. Fetch User from MongoDB
    Optional<Users> optionalUser = Ur.findByUsername(username);
    if (optionalUser.isEmpty()) {
        throw new RuntimeException("User not found with username: " + username);
    }
    Users existingUser = optionalUser.get();
    LocalDateTime currentDate =  LocalDateTime.now();
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String publishTime = currentDate.format(timeFormatter);
    // 2. Save Article in MongoDB
    Articles newArticle = new Articles();
    newArticle.setAuthor(existingUser.getUsername());
    newArticle.setTitle(request.getTitle());
    newArticle.setContent(request.getContent());
    newArticle.setPublish_time(publishTime);

    Articles savedArticle = Ar.save(newArticle);

    // 3. Prepare Outbox event to create ArticleNode in Neo4j and link it to UsersNode
    Map<String, Object> neo4jPayload = new HashMap<>();
    neo4jPayload.put("articleId", savedArticle.get_id());
    neo4jPayload.put("title", savedArticle.getTitle());
    neo4jPayload.put("author", savedArticle.getAuthor());
    neo4jPayload.put("username", existingUser.getUsername()); // to find UsersNode in Neo4j

    OutboxEvent neo4jArticleCreatedEvent = new OutboxEvent();
    neo4jArticleCreatedEvent.setEventType("Neo4jArticleCreated");
    neo4jArticleCreatedEvent.setAggregateId(savedArticle.get_id());
    neo4jArticleCreatedEvent.setPayload(objectMapper.writeValueAsString(neo4jPayload));
    neo4jArticleCreatedEvent.setPublished(false);
    neo4jArticleCreatedEvent.setCreatedAt(LocalDateTime.now());

    outboxEventRepository.save(neo4jArticleCreatedEvent);

    // 4. Return saved article immediately
    return CompletableFuture.completedFuture(savedArticle);
}

    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Articles> modifyArticle(String username, String articleId, createArticleRequest request) throws JsonProcessingException{
        Optional<Users> optionalUser = Ur.findByUsername(username);
        Optional<Articles> optionalArticle = Ar.findById(articleId);
        if(optionalUser.isPresent() && optionalArticle.isPresent()){
            Articles existingArticle = optionalArticle.get();
            Users existingUser = optionalUser.get();
            if(existingArticle.getAuthor().equals(existingUser.getUsername())){
                existingArticle.setContent(request.getContent());
                existingArticle.setTitle(request.getTitle());
                Date newPublishTime = new Date();
                existingArticle.setPublish_time(newPublishTime.toString());
                Ar.save(existingArticle);
                //prepare Outbox event to update ArticleNode in Neo4j
                Map<String, Object> neo4jPayload = new HashMap<>();
                neo4jPayload.put("articleId", existingArticle.get_id());
                neo4jPayload.put("title", existingArticle.getTitle());

                OutboxEvent neo4jArticleUpdateEvent = new OutboxEvent();
                neo4jArticleUpdateEvent.setEventType("Neo4jArticleUpdated");
                neo4jArticleUpdateEvent.setAggregateId(existingArticle.get_id());
                neo4jArticleUpdateEvent.setPayload(objectMapper.writeValueAsString(neo4jPayload));
                neo4jArticleUpdateEvent.setPublished(false);
                neo4jArticleUpdateEvent.setCreatedAt(LocalDateTime.now());
                
                outboxEventRepository.save(neo4jArticleUpdateEvent);
                return CompletableFuture.completedFuture(existingArticle);
            }
            else{
                throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
            }
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
        }
        
    }
    
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> deleteArticle(String username, String articleId ) throws JsonProcessingException{
        Optional<Users> optionalUser = Ur.findByUsername(username);
        Optional<Articles> optionalArticle = Ar.findById(articleId);
        if(optionalUser.isPresent() && optionalArticle.isPresent()){
            Articles existingArticle = optionalArticle.get();
            Users existingUser = optionalUser.get();
            if(existingArticle.getAuthor().equals(existingUser.getUsername())){
                Ar.deleteById(articleId);
                //prepare Outbox event to delete ArticleNode in Neo4j
                Map<String, Object> neo4jPayload = new HashMap<>();
                neo4jPayload.put("articleId", existingArticle.get_id());
                neo4jPayload.put("username", username);
                OutboxEvent neo4jArticleDeleteEvent = new OutboxEvent();
                neo4jArticleDeleteEvent.setEventType("Neo4jArticleDeleted");
                neo4jArticleDeleteEvent.setAggregateId(articleId);
                neo4jArticleDeleteEvent.setPayload(objectMapper.writeValueAsString(neo4jPayload));
                neo4jArticleDeleteEvent.setPublished(false);
                neo4jArticleDeleteEvent.setCreatedAt(LocalDateTime.now());
                outboxEventRepository.save(neo4jArticleDeleteEvent);
                return CompletableFuture.completedFuture("Request submitted...");
            }
            else{
                throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
            }
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
        }
    }
    //OPERATIONS TO MANAGE AUTHENTICATION
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Users> registerUser(String username, String password, String nationality,
    String email) throws JsonProcessingException {
        createUserRequest request = new createUserRequest(
            username,
            password,
            List.of(ROLES.ROLE_USER),
            nationality,
            email
        );
        return createUser(request);
    }
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Users> registerAdmin(String username, String password, String nationality,
    String email) throws JsonProcessingException {
        createUserRequest request = new createUserRequest(
            username,
            password,
            List.of(ROLES.ROLE_ADMIN),
            nationality,
            email
        );
        return createUser(request);
    }

    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> changePassword(String username,String oldPassword, String newPassword){
        Optional<Users> optional = Ur.findByUsername(username);
        if(optional.isPresent()){
            Users existing = optional.get();
            if(passwordEncoder.matches(oldPassword, existing.getPassword())){
                existing.setPassword(passwordEncoder.encode(newPassword));
                Ur.save(existing);
                return CompletableFuture.completedFuture("Password Changed Correctly!");
            }
            else{
                throw new RuntimeErrorException(null, "Wrong Password, try again!");
            }
        }
        else{
            throw new RuntimeErrorException(null, "User not found in MongoDB");
        }
    }
    
    
}
