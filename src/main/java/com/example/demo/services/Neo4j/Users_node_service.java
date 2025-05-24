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
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.OutboxEvent;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.Users;
import com.example.demo.models.Neo4j.ArticlesNode;
import com.example.demo.models.Neo4j.PlayersNode;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.projections.PlayersNodeDTO;
import com.example.demo.projections.UsersNodeProjection;
import com.example.demo.relationships.has_in_F_team;
import com.example.demo.relationships.has_in_M_team;
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
    public UsersNode getUsers(String userName) {
        Optional<UsersNode> optionalUser = Unr.findByUserName(userName);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new RuntimeErrorException(null, "User not found with username: " + userName);
        }
    }

    public Page<UsersNode> getAllUsers(PageRequest page){
        return Unr.findAll(page);
    }

    public List<FifaStatsPlayer> ShowUserMPlayersStats(String username) {
        List<FifaStatsPlayer> listToReturn = new ArrayList<>();
        Optional<UsersNode> userNodeOpt = Unr.findByUserName(username);
        if (userNodeOpt.isEmpty()) {
            throw new IllegalArgumentException("User : " + username + " not found");
        }

        UsersNode user = userNodeOpt.get();
        List<has_in_M_team> relationships = user.getPlayersMNodes();

        for (has_in_M_team fifaRel : relationships) {
            String mongoId = fifaRel.getPlayer().getMongoId();
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
        Optional<UsersNode> userNodeOpt = Unr.findByUserName(username);
        if (userNodeOpt.isEmpty()) {
            throw new IllegalArgumentException("User with id: " + username + " not found");
        }

        UsersNode user = userNodeOpt.get();
        List<has_in_F_team> relationships = user.getPlayersFNodes();

        for (has_in_F_team fifaRel : relationships) {
            String mongoId = fifaRel.getPlayer().getMongoId();
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
        Optional<UsersNode> userNodeOpt = Unr.findByUserName(username);
        if(userNodeOpt.isPresent()){
            UsersNode user = userNodeOpt.get();
            List<has_in_M_team> relationships = user.getPlayersMNodes();
            List<PlayersNodeDTO> playersList = new ArrayList<>();
            for (has_in_M_team relationship : relationships) {
                PlayersNode player = relationship.getPlayer();
                PlayersNodeDTO playerProjection = new PlayersNodeDTO();
                playerProjection.setLongName(player.getLongName());
                playerProjection.setMongoId(player.getMongoId());
                playerProjection.setGender(player.getGender());
                playersList.add(playerProjection);
            }
            return playersList;
        }
        else{
            throw new IllegalArgumentException("User with id: " + username + " not found");
        }
        
}
    
    public List<PlayersNodeDTO> ShowUserFPlayers(String username) {
        Optional<UsersNode> userNodeOpt = Unr.findByUserName(username);
        if(userNodeOpt.isPresent()){
            UsersNode user = userNodeOpt.get();
            List<has_in_F_team> relationships = user.getPlayersFNodes();
            List<PlayersNodeDTO> playersList = new ArrayList<>();
            for (has_in_F_team relationship : relationships) {
                PlayersNode player = relationship.getPlayer();
                PlayersNodeDTO playerProjection = new PlayersNodeDTO();
                playerProjection.setLongName(player.getLongName());
                playerProjection.setMongoId(player.getMongoId());
                playerProjection.setGender(player.getGender());
                playersList.add(playerProjection);
            }
            return playersList;
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
    
    public Page<ArticlesNode> getUserArticles(String username, PageRequest page){
        
    return AR.findAllByAuthor(username, page);
       
    }
    
    public ArticlesNode getSpecificUserArticle(String username, String articleId){  
        Optional<ArticlesNode> optionalArticleNode = AR.findOneByAuthorAndMongoId(username, articleId);
        if (optionalArticleNode.isPresent()) {
            return optionalArticleNode.get();
        } else {
            throw new RuntimeException("Article with id: " + articleId + " not found for user: " + username);
        }
    }
    //OPERATIONS TO MANAGE PLAYERS IN THE TEAM OF A USER
    
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> addInMTeam(String username, String playerMongoId, Integer fifaVersion) throws JsonProcessingException {
    Map<String, Object> neo4jPayload = new HashMap<>();
    neo4jPayload.put("username", username);
    neo4jPayload.put("playersMongoId", playerMongoId);
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


    
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> addInFTeam(String username, String playerMongoId, Integer fifaVersion) throws JsonProcessingException {
    Map<String, Object> neo4jPayload = new HashMap<>();
    neo4jPayload.put("username", username);
    neo4jPayload.put("playersMongoId", playerMongoId);
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

     
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> removePlayerMTeam(String username, String playerMongoId) throws JsonProcessingException {
    Map<String, Object> neo4jPayload = new HashMap<>();
    neo4jPayload.put("username", username);
    neo4jPayload.put("playersMongoId", playerMongoId);
    
    OutboxEvent event = new OutboxEvent();
    event.setEventType("Neo4jRemoveInMTeam");
    event.setAggregateId(playerMongoId);
    event.setCreatedAt(LocalDateTime.now());
    event.setPayload(objectMapper.writeValueAsString(neo4jPayload));
    event.setPublished(false);
    outboxEventRepository.save(event);
    return CompletableFuture.completedFuture("Request submitted...");
    }

    
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> removePlayerFTeam(String username, String playerMongoId) throws JsonProcessingException{
        Map<String, Object> neo4jPayload = new HashMap<>();
        neo4jPayload.put("username", username);
        neo4jPayload.put("playersMongoId", playerMongoId);
    
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
    
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> LIKE_ARTICLE(String username, String articleId) throws JsonProcessingException {
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
        return CompletableFuture.completedFuture("Request submitted...");
    
        } 


    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> UNLIKE_ARTICLE(String username, String articleId) throws JsonProcessingException {
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
        return CompletableFuture.completedFuture("Request submitted...");
            
        } 

  
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> FOLLOW(String Username, String targetUsername) throws JsonProcessingException {
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
        return CompletableFuture.completedFuture("Request submitted...");
    
    
    } 

    
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> UNFOLLOW(String Username, String targetUsername) throws JsonProcessingException {
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
        return CompletableFuture.completedFuture("Request submitted...");
            
    } 
    
    
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> team_LIKE(String username, String teamMongoId) throws JsonProcessingException {
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
        return CompletableFuture.completedFuture("Request submitted...");
        
    } 
     
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> team_UNLIKE(String username, String teamMongoId) throws JsonProcessingException {
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
        return CompletableFuture.completedFuture("Request submitted...");
        } 
    
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> coach_LIKE(String username, String coachMongoId) throws JsonProcessingException {
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
        return CompletableFuture.completedFuture("Request submitted...");
            
        } 
    
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> coach_UNLIKE(String username, String coachMongoId) throws JsonProcessingException {
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
        return CompletableFuture.completedFuture("Request submitted...");
        } 

    
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> player_LIKE(String username, String playerMongoId) throws JsonProcessingException {
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
        return CompletableFuture.completedFuture("Request submitted...");

            
        } 
    
    @Async("customAsyncExecutor")
    @Retryable(
    value = { Exception.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> player_UNLIKE(String username, String playerMongoId) throws JsonProcessingException {
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
        return CompletableFuture.completedFuture("Request submitted...");
           
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

    public void deleteUser(String mongoId) {
        Optional<UsersNode> optionalUserNode = Unr.findByMongoId(mongoId);
        if(optionalUserNode.isPresent()){
            UsersNode existingUsersNode = optionalUserNode.get();
            existingUsersNode.getPlayersFNodes().clear();
            existingUsersNode.getPlayersMNodes().clear();
            existingUsersNode.getArticlesNodes().clear();
            existingUsersNode.getFollowings().clear();
            existingUsersNode.getLikedArticlesNodes().clear();
            existingUsersNode.getCoachesNodes().clear();
            existingUsersNode.getTeamsNodes().clear();
            existingUsersNode.getPlayersMNodes().clear();

            Unr.delete(existingUsersNode);

        }
        else{
            throw new RuntimeErrorException(null, "User not found with mongoId: " + mongoId);
        }
    }

    private void ensureUserNodeIndexes() {
        neo4jClient.query("""
            CREATE INDEX mongoId IF NOT EXISTS FOR (u:UsersNode) ON (u.mongoId)
        """).run();
        
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

