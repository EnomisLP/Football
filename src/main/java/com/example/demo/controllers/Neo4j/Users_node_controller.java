package com.example.demo.controllers.Neo4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.Neo4j.ArticlesNode;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.projections.UsersNodeProjection;
import com.example.demo.relationships.has_in_F_team;
import com.example.demo.relationships.has_in_M_team;
import com.example.demo.services.Neo4j.Users_node_service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/v1/Users_Node")
@Tag(name = "Users_node", description = "QUERIES AND AGGREGATION FOR USERS_NODE")
public class Users_node_controller {

    @Autowired
    private final Users_node_service Uns;
    private final AuthenticationManager authenticationManager;

    public Users_node_controller(Users_node_service uns, AuthenticationManager a) {
        this.Uns = uns;
        this.authenticationManager = a;
    }

    // READ
    @GetMapping("/user/{id}")
    @Operation(summary = "READ operation: get users_nodes")
    public UsersNode getUser(@PathVariable Long id) {
        return Uns.getUsers(id);
    }

    @GetMapping("/admin")
    @Operation(summary = "READ: get all Users_node")
    public Page<UsersNode> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return Uns.getAllUsers(pageable);
    }

    @GetMapping("/user/articles")
    @Operation(summary = "READ: get all articles of a user")
    public Page<ArticlesNode> getUserArticles(Authentication auth,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return Uns.getUserArticles(auth.getName(), pageable);
    }

    @GetMapping("/user/articles/{articleId}")
    @Operation(summary = "READ: get a specific article of a user")
    public ArticlesNode getUserArticle(Authentication auth, @PathVariable Long articleId) {
        return Uns.getSpecificUserArticle(auth.getName(), articleId);
    }

    @GetMapping("/user/fifaMStats")
    public List<FifaStatsPlayer> getFifaMStats(Authentication auth) {
        return Uns.ShowUserMPlayersStats(auth.getName());
    }

    @GetMapping("/user/fifaFStats")
    public List<FifaStatsPlayer> getFifaFStats(Authentication auth) {
        return Uns.ShowUserFPlayersStats(auth.getName());
    }

    @GetMapping("/user/MPlayers")
    public List<has_in_M_team> geMPlayers(Authentication auth) {
        return Uns.ShowUserMPlayers(auth.getName());
    }

    @GetMapping("/user/FPlayers")
    public List<has_in_F_team> getFPlayers(Authentication auth) {
        return Uns.ShowUserFPlayers(auth.getName());
    }

    @GetMapping("/user/followings")
    public List<UsersNodeProjection> getFollowings(Authentication auth) {
        return Uns.getFollowings(auth.getName());
    }

    @GetMapping("/user/followed")
    public List<UsersNodeProjection> getFollowedBy(Authentication auth) {
        return Uns.getFollowedBy(auth.getName());
    }

    // MAP USERS
    @PutMapping("/admin")
    @Operation(summary = "Map all the Users from MongoDB to Neo4j")
    public String MapAllUsers() {
        return Uns.mapAllUsersToNeo4j();
    }
    @PutMapping("/admin/users/likes/players/populate_neo4j")
    public String populateLikesToPlayer() {
        return Uns.populateLikesToPlayer();
    }
    
    @PutMapping("/admin/users/likes/teams/populate_neo4j")
    public String populateLikesToTeams() {
        return Uns.populateLikesToTeams();
    }
    
    @PutMapping("/admin/users/likes/coaches/populate_neo4j")
    public String populateLikesToCoaches() {
        return Uns.populateLikesToCoaches();
    }
    
    @PutMapping("/admin/users/follow/users/populate_neo4j")
    public String populateFollowsToUsers() {
        return Uns.populateFollowsToUsers();
    }

    // DELETE PLAYERS
    @DeleteMapping("/user/Mplayers/{playerName}")
    public void removeMPlayer(@PathVariable String playerName, Authentication auth) {
        Uns.removePlayerMTeam(auth.getName(), playerName);
    }

    @DeleteMapping("/user/Fplayers/{playerName}")
    public void removeFPlayer(@PathVariable String playerName, Authentication auth) {
        Uns.removePlayerFTeam(auth.getName(), playerName);
    }

    // FOLLOW / UNFOLLOW
    @PutMapping("/user/follow/{target}")
    public CompletableFuture<String> FOLLOW(Authentication auth, @PathVariable String target) {
        return Uns.FOLLOW(auth.getName(), target);
    }

    @DeleteMapping("/user/unfollow/{target}")
    public CompletableFuture<String> UNFOLLOW(Authentication auth, @PathVariable String target) {
        return Uns.UNFOLLOW(auth.getName(), target);
    }

    //LIKE / UNLIKE ARTICLES
    @PostMapping("/user/like/article/{articleId}")
    public CompletableFuture<String> articleLIKE(@PathVariable Long articleId, Authentication auth) {
        return Uns.LIKE_ARTICLE(auth.getName(), articleId);
    }

    @DeleteMapping("/user/unlike/article/{articleId}")
    public CompletableFuture<String> articleUNLIKE(@PathVariable Long articleId, Authentication auth) {
        return Uns.UNLIKE_ARTICLE(auth.getName(), articleId);
    }
    
    // ADD TO TEAM
    @PostMapping("/user/MaleTeam/{playerName}/{fifaValue}")
    public String add_in_M_Team(@PathVariable String playerName, @PathVariable int fifaValue, Authentication auth) {
        return Uns.addInMTeam(auth.getName(), playerName, fifaValue);
    }

    @PostMapping("/user/FemaleTeam/{playerName}/{fifaValue}")
    public String add_in_F_Team(@PathVariable String playerName, @PathVariable int fifaValue, Authentication auth) {
        return Uns.addInFTeam(auth.getName(), playerName, fifaValue);
    }

    // LIKES
    @PostMapping("/user/like/team/{teamName}")
    public CompletableFuture<String> teamLIKE(@PathVariable String teamName, Authentication auth) {
        return Uns.team_LIKE(auth.getName(), teamName);
    }

    @DeleteMapping("/user/unlike/team/{teamName}")
    public CompletableFuture<String> teamUNLIKE(@PathVariable String teamName, Authentication auth) {
        return Uns.team_UNLIKE(auth.getName(), teamName);
    }

    @PostMapping("/user/like/player/{playerName}")
    public CompletableFuture<String> playerLIKE(@PathVariable String playerName, Authentication auth) {
        return Uns.player_LIKE(auth.getName(), playerName);
    }

    @DeleteMapping("/user/unlike/player/{playerName}")
    public CompletableFuture<String> playerUNLIKE(@PathVariable String playerName, Authentication auth) {
        return Uns.player_UNLIKE(auth.getName(), playerName);
    }

    @PostMapping("/user/like/coach/{coachName}")
    public CompletableFuture<String> coachLIKE(@PathVariable String coachName, Authentication auth) {
        return Uns.coach_LIKE(auth.getName(), coachName);
    }

    @DeleteMapping("/user/unlike/coach/{coachName}")
    public CompletableFuture<String> coachUNLIKE(@PathVariable String coachName, Authentication auth) {
        return Uns.coach_UNLIKE(auth.getName(), coachName);
    }
    
}
