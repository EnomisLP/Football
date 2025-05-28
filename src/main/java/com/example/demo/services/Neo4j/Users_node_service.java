package com.example.demo.services.Neo4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.OutboxEvent;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.Users;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.projections.ArticlesNodeDTO;
import com.example.demo.projections.PlayersNodeDTO;
import com.example.demo.projections.UsersNodeDTO;
import com.example.demo.projections.UsersNodeProjection;
import com.example.demo.repositories.MongoDB.OutboxEventRepository;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.MongoDB.Users_repository;
import com.example.demo.repositories.Neo4j.Articles_node_rep;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.repositories.Neo4j.Players_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.repositories.Neo4j.Users_node_rep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;



@Service
public class Users_node_service {

    
    private final Users_node_rep Unr;
    private final Users_repository Ur;
    private final Players_node_rep PMNr;
    private final Players_repository PMr;
    private final Teams_node_rep TMn;
    private final Coaches_node_rep CMn;
    private static final AtomicInteger interactionCounter = new AtomicInteger(0);
    private final Articles_node_rep AR;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    private Neo4jClient neo4jClient;


    public Users_node_service(Users_node_rep unr, Users_repository ur, Players_node_rep pmnr,
        Teams_node_rep tmn, Players_repository pmr, Coaches_node_rep cmn, Articles_node_rep ar,
        OutboxEventRepository oE, ObjectMapper oM) {
        this.Unr = unr;
        this.Ur = ur;
        this.PMNr = pmnr;
        this.PMr = pmr;
        this.TMn = tmn;
        this.CMn = cmn;
        this.AR = ar;
        this.outboxEventRepository = oE;
        this.objectMapper = oM;
    }



    //------------------------- READ--------------------------
    public UsersNodeDTO getUsers(String userName) {
        return Unr.findByUserNameLight(userName).orElseThrow(() -> new RuntimeException("User not found with username: " + userName));  
    }

    public Page<UsersNodeDTO> getAllUsers(PageRequest page){
        return Unr.findAllLightWithPagination(page);
    }

    public List<FifaStatsPlayer> ShowUserMPlayersStats(String username) {
        List<FifaStatsPlayer> listToReturn = new ArrayList<>();
        Optional<UsersNodeDTO> userNodeOpt = Unr.findByUserNameLight(username);
        if (userNodeOpt.isEmpty()) {
            throw new IllegalArgumentException("User : " + username + " not found");
        }

       
        List<PlayersNodeDTO> relationships = Unr.findHasInMTeamRelationshipsByUsername(username);

        for (PlayersNodeDTO fifaRel : relationships) {
            String mongoId = fifaRel.getMongoId();
            Integer fifaVersion = fifaRel.getFifaV();

            Optional<Players> playerOpt = PMr.findPlayerWithFifaStats(mongoId, fifaVersion);
            if (playerOpt.isEmpty()) {
                System.err.println("WARNING: No FIFA stats found for Player ID: " + mongoId + " and FIFA Version: " + fifaVersion);
                continue;
            }

            Players player = playerOpt.get();
            if (player.getFifaStats() != null && !player.getFifaStats().isEmpty()) {
                listToReturn.add(player.getFifaStats().get(0));
            } else {
                System.err.println("WARNING: FIFA stats list is empty for Player ID: " + mongoId);
            }
        }

        return listToReturn;
    }

    public List<FifaStatsPlayer> ShowUserFPlayersStats(String username) {
        List<FifaStatsPlayer> listToReturn = new ArrayList<>();
        Optional<UsersNodeDTO> userNodeOpt = Unr.findByUserNameLight(username);
        if (userNodeOpt.isEmpty()) {
            throw new IllegalArgumentException("User with id: " + username + " not found");
        }

        
        List<PlayersNodeDTO> relationships = Unr.findHasInFTeamRelationshipsByUsername(username);

        for (PlayersNodeDTO fifaRel : relationships) {
            String mongoId = fifaRel.getMongoId();
            Integer fifaVersion = fifaRel.getFifaV();

            Optional<Players> playerOpt = PMr.findPlayerWithFifaStats(mongoId, fifaVersion);
            if (playerOpt.isEmpty()) {
                System.err.println("WARNING: No FIFA stats found for Player ID: " + mongoId + " and FIFA Version: " + fifaVersion);
                continue;
            }

            Players player = playerOpt.get();
            if (player.getFifaStats() != null && !player.getFifaStats().isEmpty()) {
                listToReturn.add(player.getFifaStats().get(0));
            } else {
                System.err.println("WARNING: FIFA stats list is empty for Player ID: " + mongoId);
            }
        }

        return listToReturn;
    }

    public List<PlayersNodeDTO> ShowUserMPlayers(String username) {
        Optional<UsersNodeDTO> userNodeOpt = Unr.findByUserNameLight(username);
        if(userNodeOpt.isPresent()){
            return Unr.findHasInMTeamRelationshipsByUsername(username);
        }
        else{
            throw new IllegalArgumentException("User with id: " + username + " not found");
        }
        
}
    
    public List<PlayersNodeDTO> ShowUserFPlayers(String username) {
        Optional<UsersNodeDTO> userNodeOpt = Unr.findByUserNameLight(username);
        if(userNodeOpt.isPresent()){
           return Unr.findHasInFTeamRelationshipsByUsername(username);
        }
        else{
            throw new IllegalArgumentException("User with id: " + username + " not found");
        }
    }

    public List<UsersNodeProjection> getFollowings(String username) {
        return Unr.findFollowingsByUserName(username);
    }
    
    public List<UsersNodeProjection> getFollowedBy(String username) {
        return Unr.findFollowersByUserName(username);
    }
    
    public Page<ArticlesNodeDTO> getUserArticles(String username, PageRequest page){
        
    return AR.findAllByAuthorWithPaginationLight(username, page);
       
    }

    public ArticlesNodeDTO getSpecificUserArticle(String username, String articleId){  
        Optional<ArticlesNodeDTO> optionalArticleNode = AR.findByMongoIdLight(articleId);
        if (optionalArticleNode.isPresent()) {
            ArticlesNodeDTO articleNodeDTO = optionalArticleNode.get();
            if(articleNodeDTO.getAuthor().equals(username)){
                return articleNodeDTO;
            }
            else {
                throw new RuntimeException("Article with id: " + articleId + " does not belong to user: " + username);
            }
        } else {
            throw new RuntimeException("Article with id: " + articleId + " not found for user: " + username);
        }
    }
    //OPERATIONS TO MANAGE PLAYERS IN THE TEAM OF A USER
    
    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> addInMTeam(String username, String playerMongoId, Integer fifaVersion) throws JsonProcessingException {
    Map<String, Object> neo4jPayload = new HashMap<>();
    neo4jPayload.put("username", username);
    neo4jPayload.put("playerMongoId", playerMongoId);
    neo4jPayload.put("fifaVersion", fifaVersion);
    OutboxEvent event = new OutboxEvent();
    event.setEventType("Neo4jAddInMTeam");
    event.setAggregateId(playerMongoId);
    event.setCreatedAt(LocalDateTime.now());
    event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
    event.setPublished(false);
    outboxEventRepository.save(event);
    return CompletableFuture.completedFuture("Request submitted...");
    }


    
    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> addInFTeam(String username, String playerMongoId, Integer fifaVersion) throws JsonProcessingException {
    Map<String, Object> neo4jPayload = new HashMap<>();
    neo4jPayload.put("username", username);
    neo4jPayload.put("playerMongoId", playerMongoId);
    neo4jPayload.put("fifaVersion", fifaVersion);
    OutboxEvent event = new OutboxEvent();
    event.setEventType("Neo4jAddInFTeam");
    event.setAggregateId(playerMongoId);
    event.setCreatedAt(LocalDateTime.now());
    event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
    event.setPublished(false);
    outboxEventRepository.save(event);
    return CompletableFuture.completedFuture("Request submitted...");
    
    }

     
    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> removePlayerMTeam(String username, String playerMongoId) throws JsonProcessingException {
    Map<String, Object> neo4jPayload = new HashMap<>();
    neo4jPayload.put("username", username);
    neo4jPayload.put("playerMongoId", playerMongoId);
    
    OutboxEvent event = new OutboxEvent();
    event.setEventType("Neo4jRemoveInMTeam");
    event.setAggregateId(playerMongoId);
    event.setCreatedAt(LocalDateTime.now());
    event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
    event.setPublished(false);
    outboxEventRepository.save(event);
    return CompletableFuture.completedFuture("Request submitted...");
    }

    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> removePlayerFTeam(String username, String playerMongoId) throws JsonProcessingException{
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", username);
        neo4jPayload.put("playerMongoId", playerMongoId);
    
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jRemoveInFTeam");
        event.setAggregateId(playerMongoId);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);
        return CompletableFuture.completedFuture("Request submitted...");
    }

    //----------------USER INTERACTIONS------------------
    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void LIKE_ARTICLE(String username, String articleId) throws JsonProcessingException {
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", username);
        neo4jPayload.put("articleId", articleId);
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jLikeArticle");
        event.setAggregateId(articleId);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);
    
        } 


    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void UNLIKE_ARTICLE(String username, String articleId) throws JsonProcessingException {
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", username);
        neo4jPayload.put("articleId", articleId);
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jUnlikeArticle");
        event.setAggregateId(articleId);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);
            
        } 

  
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void FOLLOW(String Username, String targetUsername) throws JsonProcessingException {
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", Username);
        neo4jPayload.put("targetUsername", targetUsername);
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jFollowUser");
        event.setAggregateId(targetUsername);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);
    
    } 

    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void UNFOLLOW(String Username, String targetUsername) throws JsonProcessingException {
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", Username);
        neo4jPayload.put("targetUsername", targetUsername);
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jUnfollowUser");
        event.setAggregateId(targetUsername);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);
            
    } 
    
    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void team_LIKE(String username, String teamMongoId) throws JsonProcessingException {
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", username);
        neo4jPayload.put("teamMongoId", teamMongoId);
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jLikeTeam");
        event.setAggregateId(teamMongoId);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);
    } 
     

    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void team_UNLIKE(String username, String teamMongoId) throws JsonProcessingException {
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", username);
        neo4jPayload.put("teamMongoId", teamMongoId);
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jUnlikeTeam");
        event.setAggregateId(teamMongoId);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);
        } 
    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void coach_LIKE(String username, String coachMongoId) throws JsonProcessingException {
        System.out.println("Attempting to find user: " + username);
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", username);
        neo4jPayload.put("coachMongoId", coachMongoId);
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jLikeCoach");
        event.setAggregateId(coachMongoId);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);
            
        } 
    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void coach_UNLIKE(String username, String coachMongoId) throws JsonProcessingException {
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", username);
        neo4jPayload.put("coachMongoId", coachMongoId);
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jUnlikeCoach");
        event.setAggregateId(coachMongoId);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);
        } 

    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void player_LIKE(String username, String playerMongoId) throws JsonProcessingException {
            Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", username);
        neo4jPayload.put("playerMongoId", playerMongoId);
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jLikePlayer");
        event.setAggregateId(playerMongoId);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);

            
        } 
    
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void player_UNLIKE(String username, String playerMongoId) throws JsonProcessingException {
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", username);
        neo4jPayload.put("playerMongoId", playerMongoId);
        OutboxEvent event = new OutboxEvent();
        event.setEventType("Neo4jUnlikePlayer");
        event.setAggregateId(playerMongoId);
        event.setCreatedAt(LocalDateTime.now());
        event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
        event.setPublished(false);
        outboxEventRepository.save(event);
           
        } 
    
    //--------------------------ADMIN--------------------------
    public static String getLoggedInUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername(); // This is the logged-in user's username
        } else {
            return principal.toString();
        }
    }

    public void deleteUser(String userName) {
        Optional<UsersNodeDTO> optionalUserNode = Unr.findByUserNameLight(userName);
        if(optionalUserNode.isPresent()){
            Unr.deleteUserByUserNameLight(userName);

        }
        else{
            throw new RuntimeErrorException(null, "User not found with userName: " + userName);
        }
    }

    private void ensureUserNodeIndexes() {
        
        neo4jClient.query("""
            CREATE INDEX userName IF NOT EXISTS FOR (u:UsersNode) ON (u.userName)
        """).run();
        
    }
   
    @Transactional
    public String doMapAllUsersToNeo4j(){
        List<Users> AllUsers = Ur.findAll();
        List<UsersNode> usersToAdd = new ArrayList<>();
        System.out.println("Users found :" + AllUsers.size());
        for (Users user : AllUsers) {
            if (Unr.existsByMongoId(user.get_id())) {
                continue;
            }
           
            UsersNode userNode = new UsersNode();
            userNode.setMongoId(user.get_id());
            userNode.setUserName(user.getUsername());
            usersToAdd.add(userNode);
        }
        Unr.saveAll(usersToAdd);
        return "The amount of UsersNode created are: " + usersToAdd.size();
    }
    public String mapAllUsersToNeo4j() {
        ensureUserNodeIndexes(); // Ensure indexes are created before mapping users
        return doMapAllUsersToNeo4j();
    }

    // Utility
    public String populateFollowsToUsers() {
        
        String dataPath="data/user_follows.csv";
        
        try (BufferedReader br = new BufferedReader(new FileReader(dataPath))) {
            String line;
            //skip first line
            line=br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                this.Unr.createFollowRelation(values[0],values[1]);
            }
        }catch(Exception e){
          System.out.println(e);
        }
        
        return "Finished";
    }
    
    public String populateLikesToPlayer() {
        
        String dataPath="data/players_likes.csv";
        
        try (BufferedReader br = new BufferedReader(new FileReader(dataPath))) {
            String line;
            //skip first line
            line=br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                this.Unr.createLikeRelationToPlayer(values[0],values[1]);
            }
        }catch(Exception e){
          System.out.println(e);  
        }
        
        return "Finished";
    }
    
    public String populateLikesToTeams() {
        
        String dataPath="data/teams_likes.csv";
        
        try (BufferedReader br = new BufferedReader(new FileReader(dataPath))) {
            String line;
            //skip first line
            line=br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                this.Unr.createLikeRelationToTeam(values[0],values[1]);
            }
        }catch(Exception e){
          System.out.println(e);  
        }
        
        return "Finished";
    }
    
    public String populateLikesToCoaches() {
        
        String dataPath="data/coaches_likes.csv";
        
        try (BufferedReader br = new BufferedReader(new FileReader(dataPath))) {
            String line;
            //skip first line
            line=br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                this.Unr.createLikeRelationToCoach(values[0],values[1]);
            }
        }catch(Exception e){
          System.out.println(e);  
        }
        
        return "Finished";
    }
    
}

