package com.example.demo.services.Neo4j;

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
import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.models.Neo4j.PlayersNode;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.models.Neo4j.UsersNode;
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
    public UsersNode getUsers(Long id) {
        Optional<UsersNode> optionalUser = Unr.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new RuntimeErrorException(null, "User not found with id: " + id);
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

    public List<has_in_M_team> ShowUserMPlayers(String username) {
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        if (optionalUserNode.isPresent()) {
            UsersNode existingUsersNode = optionalUserNode.get();
            return existingUsersNode.getPlayersMNodes();
        } else {
            throw new RuntimeException("User with id: " + username + " not found");
        }
    }
    
    public List<has_in_F_team> ShowUserFPlayers(String username) {
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        if (optionalUserNode.isPresent()) {
            UsersNode existingUsersNode = optionalUserNode.get();
            return existingUsersNode.getPlayersFNodes();
        } else {
            throw new RuntimeException("User with id: " + username + " not found");
        }
    }

    public List<UsersNodeProjection> getFollowings(String username) {
        return Unr.findFollowingsByUserName(username);
    }
    
    public List<UsersNodeProjection> getFollowedBy(String username) {
        return Unr.findFollowersByUserName(username);
    }
    
    public Page<ArticlesNode> getUserArticles(String username, PageRequest page){
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        if(optionalUserNode.isPresent()){
            UsersNode existingUsersNode = optionalUserNode.get();
            return AR.findAllByUsersNode(existingUsersNode, page);
        }
        else{
            throw new RuntimeException("User with id: " + username + " not found");
        }
    }
    
    public ArticlesNode getSpecificUserArticle(String username, Long articleId){
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<ArticlesNode> optionalArticleNode = AR.findByMongoId(String.valueOf(articleId));
        if(optionalUserNode.isPresent() && optionalArticleNode.isPresent()){
            UsersNode existingUsersNode = optionalUserNode.get();
            ArticlesNode existingArticlesNode = optionalArticleNode.get();
            if(existingUsersNode.getArticlesNodes().contains(existingArticlesNode)){
                return existingArticlesNode;
            }
            else{
                throw new RuntimeException("Article with id: " + articleId + " not found for user: " + username);
            }
        }
        else{
            throw new RuntimeException("User with id: " + username + " or Article with id: " + articleId + " not found");
        }
    }
    //OPERATIONS TO MANAGE PLAYERS IN THE TEAM OF A USER
    @Transactional
    public String addInMTeam(String username, Integer playerId, Integer fifaVersion) {
    Optional<UsersNode> userNodeOpt = Unr.findByUserName(username);
    Optional<PlayersNode> playerNodeOpt = PMNr.findByPlayerId(playerId);

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
        throw new IllegalArgumentException("FIFA stats not found for Player ID: " + playerId);
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

    if( userNode.getPlayersMNodes().stream().anyMatch(rel -> rel.getPlayer().getPlayerId().equals(playerNode.getPlayerId()))){
        System.err.println("Player: " + playerNode.getMongoId()+" already in the team.");
        return "This player  already exists in the team.";
    }

        for (FifaStatsPlayer stat : fifaStats) {
            if (stat.getFifa_version().equals(fifaVersion)) {
                if(userNode.getPlayersMNodes().size() < MAX_NUMBER_TEAM){
                    has_in_M_team fifaVersionRel = new has_in_M_team(playerNode, fifaVersion);
                    userNode.getPlayersMNodes().add(fifaVersionRel);
                    //Unr.userAddMPlayer(username, playerId, fifaVersion);
                    Unr.save(userNode);
                    return "Player with ID " + playerId + " added to User " + username + "'s M team with FIFA Version " + fifaVersion;
                }
                else {
                    throw new IllegalArgumentException("Team can have maximum "+MAX_NUMBER_TEAM+" players!");
                }
            }
        }

        throw new IllegalArgumentException("FIFA version " + fifaVersion + " not found for Player ID: " + playerId);
    }
    @Transactional
    public String addInFTeam(String username, Integer playerId, Integer fifaVersion) {
    Optional<UsersNode> userNodeOpt = Unr.findByUserName(username);
    Optional<PlayersNode> playerNodeOpt = PMNr.findByPlayerId(playerId);

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
        throw new IllegalArgumentException("FIFA stats not found for Player ID: " + playerId);
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
    if( userNode.getPlayersFNodes().stream().anyMatch(rel -> rel.getPlayer().getPlayerId().equals(playerNode.getPlayerId()))){
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
                    return "Player with ID " + playerId + " added to User " + username + "'s M team with FIFA Version " + fifaVersion;
                }
                else{
                    throw new IllegalArgumentException("Team can have maximum "+MAX_NUMBER_TEAM+" players!");
                }
            }
        }

        throw new IllegalArgumentException("FIFA version " + fifaVersion + " not found for Player ID: " + playerId);
    }
    @Transactional
    public void removePlayerMTeam(String username, Integer playerId) {
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<PlayersNode> optionalPlayerNode = PMNr.findByPlayerId(playerId);
        
        if (optionalUserNode.isPresent() && optionalPlayerNode.isPresent()) {
            PlayersNode existingPlayersNode = optionalPlayerNode.get();
            UsersNode existingUsersNode = optionalUserNode.get();
            
            if(existingPlayersNode.getGender().equals("male")) {
                // Use removeIf to safely remove the players from the M team
                existingUsersNode.getPlayersMNodes().removeIf(existing -> existing.alreadyExistPlayer(existingPlayersNode));
                
                // Save the updated UsersNode
                Unr.save(existingUsersNode);
            } else {
                throw new RuntimeException("Player with ID: " + playerId + " is a female");
            }
        } else {
            throw new RuntimeException("Player with ID: " + playerId + " not found in the M team of user: " + username);
        }
    }
      @Transactional
    public void removePlayerFTeam(String username, Integer playerId){
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<PlayersNode> optionalPlayerNode = PMNr.findByPlayerId(playerId);
        if (optionalUserNode.isPresent() && optionalPlayerNode.isPresent()) {
            PlayersNode existingPlayersNode = optionalPlayerNode.get();
            UsersNode existingUsersNode = optionalUserNode.get();
            if(existingPlayersNode.getGender().equals("female")){
                // Use removeIf to safely remove the players from the M team
                existingUsersNode.getPlayersFNodes().removeIf(existing -> existing.alreadyExistPlayer(existingPlayersNode));
                
                // Save the updated UsersNode
                Unr.save(existingUsersNode);
            }
            else{
                throw new RuntimeException("Player with ID: " + playerId + " is a female");
            }
        } else {
            throw new RuntimeException("Player with ID: " + playerId + " not found in the M team of user: " + username);
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
    public CompletableFuture<String> LIKE_ARTICLE(String username, Long articleId) {
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<ArticlesNode> optionalArticleNode = AR.findByMongoId(String.valueOf(articleId));
    
        if (optionalUserNode.isPresent() && optionalArticleNode.isPresent()) {
            UsersNode usersNode = optionalUserNode.get();
            ArticlesNode articlesNode = optionalArticleNode.get();
    
            // Add the article to the user's liked articles
            usersNode.getArticlesNodes().add(articlesNode);
            Unr.save(usersNode);
    
            return CompletableFuture.completedFuture("User: " + username + " now likes article with id: " + articleId);
        } else {
            throw new RuntimeException("User: " + username + " or Article with id: " + articleId + " not found");
        }
    }


    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> UNLIKE_ARTICLE(String username, Long articleId) {
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<ArticlesNode> optionalArticleNode = AR.findByMongoId(String.valueOf(articleId));
    
        if (optionalUserNode.isPresent() && optionalArticleNode.isPresent()) {
            UsersNode existingUsersNode = optionalUserNode.get();
            ArticlesNode existingArticlesNode = optionalArticleNode.get();
    
            // Remove the article from the user's liked articles
            existingUsersNode.getArticlesNodes().remove(existingArticlesNode);
            Unr.save(existingUsersNode);
    
            return CompletableFuture.completedFuture("User: " + username + " is not liking article with id: " + articleId + " anymore");
        } else {
            throw new RuntimeException("User: " + username + " or Article with id: " + articleId + " not found");
        }
    }

    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> FOLLOW(String Username, String targetUsername) {
    if (Username.equals(targetUsername)) return CompletableFuture.completedFuture("You cannot follow yourself.");
    long startTime = System.currentTimeMillis(); // Start timer
    System.out.println("FOLLOW request started: "+ Username +" -> " + targetUsername);
    //Log to see which thread is executing the request
    // This will help in debugging and understanding the flow of execution
    System.out.println("Running in thread: " + Thread.currentThread().getName());
    interactionCounter.incrementAndGet(); // Count interaction
    Optional<UsersNode> Opt = Unr.findByUserName(Username);
    Optional<UsersNode> targetOpt = Unr.findByUserName(targetUsername);

    if (Opt.isPresent() && targetOpt.isPresent()) {
        UsersNode user = Opt.get();
        UsersNode target = targetOpt.get();

        if (!user.getFollowings().contains(target)) {
            user.getFollowings().add(target);
        }
        if (!target.getFollowers().contains(user)) {
            target.getFollowers().add(user);
        }

        Unr.save(user); 
        Unr.createFollowRelation(user.getUserName(), target.getUserName());
        long endTime = System.currentTimeMillis(); // End timer
        System.out.println("FOLLOW completed in " + (endTime - startTime) +" ms. Total calls: "+ interactionCounter.get());
        return CompletableFuture.completedFuture("User: " + Username + " now follows user: " + targetUsername);
    } else {
        throw new RuntimeException("User(s) not found");
        }
    }

    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> UNFOLLOW(String Username, String targetUsername) {
        if (Username.equals(targetUsername)) return CompletableFuture.completedFuture("You cannot unfollow yourself.");
    
        Optional<UsersNode> Opt = Unr.findByUserName(Username);
        Optional<UsersNode> targetOpt = Unr.findByUserName(targetUsername);
    
        if (Opt.isPresent() && targetOpt.isPresent()) {
            UsersNode user = Opt.get();
            UsersNode target = targetOpt.get();
    
            user.getFollowings().removeIf(u -> u.getUserName().equals(target.getUserName()));
            target.getFollowers().removeIf(u -> u.getUserName().equals(user.getUserName()));
    
            Unr.save(user); 
             //Since some times it doesn't delete the relationship       
            Unr.removeFollowingRelationship(Username, targetUsername);
            Unr.removeFollowerRelationship(Username, targetUsername);
            return CompletableFuture.completedFuture("User: " + Username + " no longer follows user: " + targetUsername);
        } else {
            throw new RuntimeException("User(s) not found");
        }
    }
    
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> team_LIKE(String username, Long teamId) {
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<TeamsNode> optionalTeamNode = TMn.findByTeamId(teamId);
    
        if (optionalUserNode.isPresent() && optionalTeamNode.isPresent()) {
            UsersNode usersNode = optionalUserNode.get();
            TeamsNode teamsNode = optionalTeamNode.get();
    
            // Add the team to the user's liked teams
            usersNode.getTeamsNodes().add(teamsNode);
            Unr.save(usersNode);
    
            return CompletableFuture.completedFuture("User: " + username + " now likes team with id: " + teamId);
        } else {
            throw new RuntimeException("User: " + username + " or Team with id: " + teamId + " not found");
        }
    }
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> team_UNLIKE(String username, Long teamId) {
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<TeamsNode> optionalTeamNode = TMn.findByTeamId(teamId);
    
        if (optionalUserNode.isPresent() && optionalTeamNode.isPresent()) {
            UsersNode existingUsersNode = optionalUserNode.get();
            TeamsNode existingTeamsNode = optionalTeamNode.get();
    
            // Remove the team from the user's liked teams
            existingUsersNode.getTeamsNodes().remove(existingTeamsNode);
            Unr.save(existingUsersNode);
    
            return CompletableFuture.completedFuture("User: " + username + " is not liking team with id: " + teamId + " anymore");
        } else {
            throw new RuntimeException("User: " + username + " or Team with id: " + teamId + " not found");
        }
    }
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> coach_LIKE(String username, Integer coachId) {
        System.out.println("Attempting to find user: " + username);
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<CoachesNode> optionalCoachNode = CMn.findByCoachId(coachId);
    
        if (optionalUserNode.isPresent() && optionalCoachNode.isPresent()) {
            UsersNode usersNode = optionalUserNode.get();
            CoachesNode coachesNode = optionalCoachNode.get();
    
            // Add the coach to the user's liked coaches
            usersNode.getCoachesNodes().add(coachesNode);
            Unr.save(usersNode);
    
            return CompletableFuture.completedFuture("User: " + username + " now likes coach with id: " + coachId);
        } else {
            throw new RuntimeException("User: " + username + " or Coach with id: " + coachId + " not found");
        }
    }
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> coach_UNLIKE(String username, Integer coachId) {
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<CoachesNode> optionalCoachNode = CMn.findByCoachId(coachId);
    
        if (optionalUserNode.isPresent() && optionalCoachNode.isPresent()) {
            UsersNode existingUsersNode = optionalUserNode.get();
            CoachesNode existingCoachesNode = optionalCoachNode.get();
    
            // Remove the coach from the user's liked coaches
            existingUsersNode.getCoachesNodes().remove(existingCoachesNode);
            Unr.save(existingUsersNode);
    
            return CompletableFuture.completedFuture("User: " + username + " is not liking coach with id: " + coachId + " anymore");
        } else {
            throw new RuntimeException("User: " + username + " or Coach with id: " + coachId + " not found");
        }
    }
    @Async("customAsyncExecutor")
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> player_LIKE(String username, Integer playerId) {
        System.out.println("Attempting to find user: " + username);
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<PlayersNode> optionalPlayerNode = PMNr.findByPlayerId(playerId);
    
        if (optionalUserNode.isPresent() && optionalPlayerNode.isPresent()) {
            UsersNode usersNode = optionalUserNode.get();
            PlayersNode playerNode = optionalPlayerNode.get();
    
            usersNode.getPlayerNodes().add(playerNode);
            Unr.save(usersNode);
    
            return CompletableFuture.completedFuture("User: " + username + " now likes player with id: " + playerId);
        } else {
            throw new RuntimeException("User: " + username + " or Player with id: " + playerId + " not found");
        }
    }
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> player_UNLIKE(String username, Integer playerId) {
        Optional<UsersNode> optionalUserNode = Unr.findByUserName(username);
        Optional<PlayersNode> optionalPlayerNode = PMNr.findByPlayerId(playerId);
    
        if (optionalUserNode.isPresent() && optionalPlayerNode.isPresent()) {
            UsersNode existingUsersNode = optionalUserNode.get();
            PlayersNode existingPlayersNode = optionalPlayerNode.get();
    
            
            existingUsersNode.getPlayerNodes().remove(existingPlayersNode);
            Unr.save(existingUsersNode);
    
            return CompletableFuture.completedFuture("User: " + username + " is not liking player with id: " + playerId + " anymore");
        } else {
            throw new RuntimeException("User: " + username + " or Player with id: " + playerId + " not found");
        }
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

    public void deleteUser(Long id) {
        Optional<UsersNode> optionalUser = Unr.findById(id);
        if (optionalUser.isPresent()) {
            UsersNode existingUsersNode = optionalUser.get();
            existingUsersNode.getFollowers().clear();
            existingUsersNode.getFollowings().clear();
            existingUsersNode.getCoachesNodes().clear();
            existingUsersNode.getPlayersFNodes().clear();
            existingUsersNode.getPlayersMNodes().clear();
            existingUsersNode.getTeamsNodes().clear();
            Unr.save(existingUsersNode);
            Unr.deleteById(id);
        } else {
            throw new RuntimeErrorException(null, "User not found with id: " + id);
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

    public String mapAllUsersToNeo4j() {
        ensureUserNodeIndexes(); // Ensure indexes are created before mapping users
        List<Users> AllUsers = Ur.findAll();
        List<UsersNode> usersToAdd = new ArrayList<>();
        System.out.println("Users found :" + AllUsers.size());
        for (Users user : AllUsers) {
            if (Unr.existsByMongoId(String.valueOf(user.get_id()))) {
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

}

