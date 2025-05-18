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
import com.example.demo.projections.PlayersNodeProjection;
import com.example.demo.projections.UsersNodeProjection;
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
    @GetMapping("/user/{username}")
    @Operation(summary = "READ operation: get users_nodes")
    public UsersNode getUser(@PathVariable String username) {
        return Uns.getUsers(username);
    }

    @GetMapping("/admin")
    @Operation(summary = "READ: get all Users_node")
    public Page<UsersNode> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return Uns.getAllUsers(pageable);
    }

    @GetMapping("/user/{userName}/articles")
    @Operation(summary = "READ: get all articles of a user")
    public Page<ArticlesNode> getUserArticles(@PathVariable String userName,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return Uns.getUserArticles(userName, pageable);
    }

    @GetMapping("/user/articles/{articleId}")
    @Operation(summary = "READ: get a specific article of a user")
    public ArticlesNode getUserArticle(Authentication auth, @PathVariable String articleId) {
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
    public List<PlayersNodeProjection> geMPlayers(Authentication auth) {
        return Uns.ShowUserMPlayers(auth.getName());
    }

    @GetMapping("/user/FPlayers")
    public List<PlayersNodeProjection> getFPlayers(Authentication auth) {
        return Uns.ShowUserFPlayers(auth.getName());
    }

    @GetMapping("/user/{userName}/followings")
    public List<UsersNodeProjection> getFollowings(@PathVariable String userName) {
        return Uns.getFollowings(userName);
    }

    @GetMapping("/user/{userName}/followed")
    public List<UsersNodeProjection> getFollowedBy(@PathVariable String userName) {
        return Uns.getFollowedBy(userName);
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
    @DeleteMapping("/user/Mplayers/{_id}")
    @Operation(summary = "DELETE a player in male Team by its mongoId")
    public void removeMPlayer(@PathVariable String _id, Authentication auth) {
        Uns.removePlayerMTeam(auth.getName(), _id);
    }

    @DeleteMapping("/user/Fplayers/{_id}")
    @Operation(summary = "DELETE a player in female Team by its mongoId")
    public void removeFPlayer(@PathVariable String _id, Authentication auth) {
        Uns.removePlayerFTeam(auth.getName(), _id);
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
    @Operation(summary = "LIKE an article by its mongoId")
    public CompletableFuture<String> articleLIKE(@PathVariable String articleId, Authentication auth) {
        return Uns.LIKE_ARTICLE(auth.getName(), articleId);
    }

    @DeleteMapping("/user/unlike/article/{articleId}")
    @Operation(summary = "UNLIKE an article by its mongoId")
    public CompletableFuture<String> articleUNLIKE(@PathVariable String articleId, Authentication auth) {
        return Uns.UNLIKE_ARTICLE(auth.getName(), articleId);
    }
    
    // ADD TO TEAM
    @PostMapping("/user/MaleTeam/{_id}/{fifaValue}")
    @Operation(summary = "ADD a player in male Team by its mongoId")
    public String add_in_M_Team(@PathVariable String _id, @PathVariable int fifaValue, Authentication auth) {
        return Uns.addInMTeam(auth.getName(), _id, fifaValue);
    }

    @PostMapping("/user/FemaleTeam/{_id}/{fifaValue}")
    @Operation(summary = "ADD a player in Female Team by its mongoId")
    public String add_in_F_Team(@PathVariable String _id, @PathVariable int fifaValue, Authentication auth) {
        return Uns.addInFTeam(auth.getName(), _id, fifaValue);
    }

    // LIKES
    @PostMapping("/user/like/team/{_id}")
    @Operation(summary = "LIKE a team by its mongoId")
    public CompletableFuture<String> teamLIKE(@PathVariable String _id, Authentication auth) {
        return Uns.team_LIKE(auth.getName(), _id);
    }

    @DeleteMapping("/user/unlike/team/{_id}")
    @Operation(summary = "UNLIKE a team by its mongoId")
    public CompletableFuture<String> teamUNLIKE(@PathVariable String _id, Authentication auth) {
        return Uns.team_UNLIKE(auth.getName(), _id);
    }

    @PostMapping("/user/like/player/{_id}")
    @Operation(summary = "LIKE a player by its mongoId")
    public CompletableFuture<String> playerLIKE(@PathVariable String _id, Authentication auth) {
        return Uns.player_LIKE(auth.getName(), _id);
    }

    @DeleteMapping("/user/unlike/player/{_id}")
    @Operation(summary = "UNLIKE a player by its mongoId")
    public CompletableFuture<String> playerUNLIKE(@PathVariable String _id, Authentication auth) {
        return Uns.player_UNLIKE(auth.getName(), _id);
    }

    @PostMapping("/user/like/coach/{_id}")
    @Operation(summary = "LIKE a coach by its mongoId")
    public CompletableFuture<String> coachLIKE(@PathVariable String _id, Authentication auth) {
        return Uns.coach_LIKE(auth.getName(), _id);
    }

    @DeleteMapping("/user/unlike/coach/{_id}")
    @Operation(summary = "UNLIKE a coach by its mongoId")
    public CompletableFuture<String> coachUNLIKE(@PathVariable String _id, Authentication auth) {
        return Uns.coach_UNLIKE(auth.getName(), _id);
    }
    
}
