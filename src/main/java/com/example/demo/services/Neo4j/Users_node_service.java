package com.example.demo.services.Neo4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import java.util.List;
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

import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.Users;
import com.example.demo.models.Neo4j.ArticlesNode;
import com.example.demo.models.Neo4j.PlayersNode;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.projections.PlayersNodeProjection;
import com.example.demo.projections.UsersNodeProjection;
import com.example.demo.relationships.has_in_F_team;
import com.example.demo.relationships.has_in_M_team;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.MongoDB.Users_repository;
import com.example.demo.repositories.Neo4j.Articles_node_rep;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.repositories.Neo4j.Players_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.repositories.Neo4j.Users_node_rep;

import jakarta.transaction.Transactional;

@Service
public class Users_node_service {

    
    private final Users_node_rep Unr;
    private final Users_repository Ur;
    private final Players_node_rep PMNr;
    private final Players_repository PMr;
    private static final Integer MAX_NUMBER_TEAM = 11;
    private final Teams_node_rep TMn;
    private final Coaches_node_rep CMn;
    private static final AtomicInteger interactionCounter = new AtomicInteger(0);
    private final Articles_node_rep AR;

    @Autowired
    private Neo4jClient neo4jClient;


    public Users_node_service(Users_node_rep unr, Users_repository ur, Players_node_rep pmnr,
        Teams_node_rep tmn, Players_repository pmr, Coaches_node_rep cmn, Articles_node_rep ar) {
        this.Unr = unr;
        this.Ur = ur;
        this.PMNr = pmnr;
        this.PMr = pmr;
        this.TMn = tmn;
        this.CMn = cmn;
        this.AR = ar;
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

    public List<PlayersNodeProjection> ShowUserMPlayers(String username) {
        Optional<UsersNode> userNodeOpt = Unr.findByUserName(username);
        if(userNodeOpt.isPresent()){
            UsersNode user = userNodeOpt.get();
            List<has_in_M_team> relationships = user.getPlayersMNodes();
            List<PlayersNodeProjection> playersList = new ArrayList<>();
            for (has_in_M_team relationship : relationships) {
                PlayersNode player = relationship.getPlayer();
                PlayersNodeProjection playerProjection = new PlayersNodeProjection();
                playerProjection.setLongName(player.getLongName());
                playerProjection.setMongoId(player.getMongoId());
                playerProjection.setAge(player.getAge());
                playerProjection.setNationalityName(player.getNationalityName());
                playerProjection.setGender(player.getGender());
                playerProjection.setFifaV(relationship.getFifaV());
                playersList.add(playerProjection);
            }
            return playersList;
        }
        else{
            throw new IllegalArgumentException("User with id: " + username + " not found");
        }
        
}
    
    public List<PlayersNodeProjection> ShowUserFPlayers(String username) {
        Optional<UsersNode> userNodeOpt = Unr.findByUserName(username);
        if(userNodeOpt.isPresent()){
            UsersNode user = userNodeOpt.get();
            List<has_in_F_team> relationships = user.getPlayersFNodes();
            List<PlayersNodeProjection> playersList = new ArrayList<>();
            for (has_in_F_team relationship : relationships) {
                PlayersNode player = relationship.getPlayer();
                PlayersNodeProjection playerProjection = new PlayersNodeProjection();
                playerProjection.setLongName(player.getLongName());
                playerProjection.setMongoId(player.getMongoId());
                playerProjection.setAge(player.getAge());
                playerProjection.setNationalityName(player.getNationalityName());
                playerProjection.setGender(player.getGender());
                playerProjection.setFifaV(relationship.getFifaV());
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
        /*Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<ArticlesNode> optionalArticleNode = AR.findByMongoId(articleId);
        if(optionalUserNode.isPresent() && optionalArticleNode.isPresent()){
            UsersNode existingUsersNode = optionalUserNode.get();
            ArticlesNode existingArticlesNode = optionalArticleNode.get();
            for(articles_wrote articlesWrote : existingUsersNode.getArticlesNodes()){
                if(articlesWrote.getArticlesNode().equals(existingArticlesNode)){
                    return existingArticlesNode;
                }
            }
            throw new RuntimeException("Article with id: " + articleId + " not found for user: " + username);
        }
        else{
            throw new RuntimeException("User with id: " + username + " or Article with id: " + articleId + " not found");
        }*/
        Optional<ArticlesNode> optionalArticleNode = AR.findOneByAuthorAndMongoId(username, articleId);
        if (optionalArticleNode.isPresent()) {
            return optionalArticleNode.get();
        } else {
            throw new RuntimeException("Article with id: " + articleId + " not found for user: " + username);
        }
    }
    //OPERATIONS TO MANAGE PLAYERS IN THE TEAM OF A USER
    @Transactional
    public String addInMTeam(String username, String playerMongoId, Integer fifaVersion) {
    Optional<UsersNode> userNodeOpt = Unr.findByUserName(username);
    Optional<PlayersNode> playerNodeOpt = PMNr.findByMongoId(playerMongoId);

    if (userNodeOpt.isEmpty() || playerNodeOpt.isEmpty()) {
        throw new IllegalArgumentException("User or Player not found.");
    }

    UsersNode userNode = userNodeOpt.get();
    PlayersNode playerNode = playerNodeOpt.get();

    if (playerNode.getGender().equals("female")) {
        throw new IllegalArgumentException("Female players can't be added into Male Team");
    }

    Optional<Players> mongoPlayerOpt = PMr.findById(playerNode.getMongoId());
    if (mongoPlayerOpt.isEmpty() || mongoPlayerOpt.get().getFifaStats().isEmpty()) {
        throw new IllegalArgumentException("FIFA stats not found for Player id: " + playerMongoId);
    }

    Players mongoPlayer = mongoPlayerOpt.get();
    List<FifaStatsPlayer> fifaStats = mongoPlayer.getFifaStats();

    // Check if the relationship with the same player and FIFA version already exists
    boolean existingRelationship = userNode.getPlayersMNodes().stream()
            .anyMatch(rel -> rel.alreadyExist(playerNode, fifaVersion));

    if (existingRelationship) {
        System.err.println("Player: " + playerNode.getMongoId() +
                           " with FIFA Version: " + fifaVersion + " already exists in the team.");
        return "This player with the same FIFA version already exists in the team.";
    }

    if( userNode.getPlayersMNodes().stream().anyMatch(rel -> rel.getPlayer().getMongoId().equals(playerNode.getMongoId()))){
        System.err.println("Player: " + playerNode.getMongoId()+" already in the team.");
        return "This player  already exists in the team.";
    }

        for (FifaStatsPlayer stat : fifaStats) {
            if (stat.getFifa_version().equals(fifaVersion)) {
                if(userNode.getPlayersMNodes().size() < MAX_NUMBER_TEAM){
                    has_in_M_team fifaVersionRel = new has_in_M_team(playerNode, fifaVersion);
                    userNode.getPlayersMNodes().add(fifaVersionRel);
                    Unr.save(userNode);
                    return "Player with id " + playerMongoId + " added to User " + username + "'s M team with FIFA Version " + fifaVersion;
                }
                else {
                    throw new IllegalArgumentException("Team can have maximum "+MAX_NUMBER_TEAM+" players!");
                }
            }
        }

        throw new IllegalArgumentException("FIFA version " + fifaVersion + " not found for Player id: " + playerMongoId);
    }
    @Transactional
    public String addInFTeam(String username, String playerMongoId, Integer fifaVersion) {
    Optional<UsersNode> userNodeOpt = Unr.findByUserName(username);
    Optional<PlayersNode> playerNodeOpt = PMNr.findByMongoId(playerMongoId);

    if (userNodeOpt.isEmpty() || playerNodeOpt.isEmpty()) {
        throw new IllegalArgumentException("User or Player not found.");
    }

    UsersNode userNode = userNodeOpt.get();
    PlayersNode playerNode = playerNodeOpt.get();

    if (playerNode.getGender().equals("male")) {
        throw new IllegalArgumentException("Female players can't be added into Male Team");
    }

    Optional<Players> mongoPlayerOpt = PMr.findById(playerNode.getMongoId());
    if (mongoPlayerOpt.isEmpty() || mongoPlayerOpt.get().getFifaStats().isEmpty()) {
        throw new IllegalArgumentException("FIFA stats not found for Player id: " + playerMongoId);
    }

    Players mongoPlayer = mongoPlayerOpt.get();
    List<FifaStatsPlayer> fifaStats = mongoPlayer.getFifaStats();

    // Check if the relationship with the same player and FIFA version already exists
    boolean existingRelationship = userNode.getPlayersMNodes().stream()
            .anyMatch(rel -> rel.alreadyExist(playerNode, fifaVersion));

    if (existingRelationship) {
        System.err.println("Player: " + playerNode.getMongoId() +
                           " with FIFA Version: " + fifaVersion + " already exists in the team.");
        return "This player with the same FIFA version already exists in the team.";
    }
    if( userNode.getPlayersFNodes().stream().anyMatch(rel -> rel.getPlayer().getMongoId().equals(playerNode.getMongoId()))){
        System.err.println("Player: " + playerNode.getMongoId()+" already in the team.");
        return "This player  already exists in the team.";
    }
        for (FifaStatsPlayer stat : fifaStats) {
            if (stat.getFifa_version().equals(fifaVersion)) {
                    if(userNode.getPlayersFNodes().size() < MAX_NUMBER_TEAM){
                    has_in_F_team fifaVersionRel = new has_in_F_team(playerNode, fifaVersion);
                    userNode.getPlayersFNodes().add(fifaVersionRel);
                    //Unr.userAddFPlayer(username, playerId, fifaVersion);
                    Unr.save(userNode);
                    return "Player with id " + playerMongoId + " added to User " + username + "'s M team with FIFA Version " + fifaVersion;
                }
                else{
                    throw new IllegalArgumentException("Team can have maximum "+MAX_NUMBER_TEAM+" players!");
                }
            }
        }

        throw new IllegalArgumentException("FIFA version " + fifaVersion + " not found for Player id: " + playerMongoId);
    }
    @Transactional
    public void removePlayerMTeam(String username, String playerMongoId) {
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<PlayersNode> optionalPlayerNode = PMNr.findByMongoId(playerMongoId);
        
        if (optionalUserNode.isPresent() && optionalPlayerNode.isPresent()) {
            PlayersNode existingPlayersNode = optionalPlayerNode.get();
            UsersNode existingUsersNode = optionalUserNode.get();
            
            if(existingPlayersNode.getGender().equals("male")) {
                // Use removeIf to safely remove the players from the M team
                existingUsersNode.getPlayersMNodes().removeIf(existing -> existing.getPlayer().getMongoId().equals(existingPlayersNode.getMongoId()));
                
                // Save the updated UsersNode
                Unr.save(existingUsersNode);
            } else {
                throw new RuntimeException("Player with id: " + playerMongoId + " is a female");
            }
        } else {
            throw new RuntimeException("Player with id: " + playerMongoId + " not found in the M team of user: " + username);
        }
    }
      @Transactional
    public void removePlayerFTeam(String username, String playerMongoId){
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<PlayersNode> optionalPlayerNode = PMNr.findByMongoId(playerMongoId);
        if (optionalUserNode.isPresent() && optionalPlayerNode.isPresent()) {
            PlayersNode existingPlayersNode = optionalPlayerNode.get();
            UsersNode existingUsersNode = optionalUserNode.get();
            if(existingPlayersNode.getGender().equals("female")){
                // Use removeIf to safely remove the players from the M team
                existingUsersNode.getPlayersFNodes().removeIf(existing -> existing.getPlayer().getMongoId().equals(existingPlayersNode.getMongoId()));  
                
                // Save the updated UsersNode
                Unr.save(existingUsersNode);
            }
            else{
                throw new RuntimeException("Player with id: " + playerMongoId + " is a female");
            }
        } else {
            throw new RuntimeException("Player with id: " + playerMongoId + " not found in the M team of user: " + username);
        }
    }

    //----------------USER INTERACTIONS------------------
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> LIKE_ARTICLE(String username, String articleId) {
        
            Unr.createLikeRelationToArticle(username, articleId);
    
            return CompletableFuture.completedFuture("User: " + username + " now likes article with id: " + articleId);
        } 


    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> UNLIKE_ARTICLE(String username, String articleId) {
        
            Unr.deleteLikeRelationToArticle(username, articleId);
            return CompletableFuture.completedFuture("User: " + username + " is not liking article with id: " + articleId + " anymore");
        } 

    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> FOLLOW(String Username, String targetUsername) {
    Unr.createFollowRelation(Username, targetUsername);
    
    return CompletableFuture.completedFuture("User: " + Username + " now follows user: " + targetUsername);
    } 

    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> UNFOLLOW(String Username, String targetUsername) {
       
            Unr.removeFollowRelationship(Username, targetUsername);
            return CompletableFuture.completedFuture("User: " + Username + " no longer follows user: " + targetUsername);
        } 
    
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> team_LIKE(String username, String teamMongoId) {
        Unr.LikeToTeam(username, teamMongoId);
        
            return CompletableFuture.completedFuture("User: " + username + " now likes team with id: " + teamMongoId);
        } 
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> team_UNLIKE(String username, String teamMongoId) {
       
            Unr.deleteLikeRelationToTeam(username, teamMongoId);
            return CompletableFuture.completedFuture("User: " + username + " is not liking team with id: " + teamMongoId + " anymore");
        } 
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> coach_LIKE(String username, String coachMongoId) {
        System.out.println("Attempting to find user: " + username);
        
            Unr.LikeToCoach(username, coachMongoId);
            return CompletableFuture.completedFuture("User: " + username + " now likes coach with id: " + coachMongoId);
        } 
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> coach_UNLIKE(String username, String coachMongoId) {
       
            Unr.deleteLikeRelationToCoach(username, coachMongoId);
            return CompletableFuture.completedFuture("User: " + username + " is not liking coach with id: " + coachMongoId + " anymore");
        } 
    @Async("customAsyncExecutor")
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> player_LIKE(String username, String playerMongoId) {
        System.out.println("Attempting to find user: " + username);
        
            Unr.LikeToPlayer(username, playerMongoId);
            return CompletableFuture.completedFuture("User: " + username + " now likes player with id: " + playerMongoId);
        } 
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> player_UNLIKE(String username, String playerMongoId) {
        
            Unr.deleteLikeRelationToPlayer(username, playerMongoId);
            return CompletableFuture.completedFuture("User: " + username + " is not liking player with id: " + playerMongoId + " anymore");
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
        if(Unr.existsByMongoId(mongoId)){
            Unr.deleteUserByMongoId(mongoId);

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

