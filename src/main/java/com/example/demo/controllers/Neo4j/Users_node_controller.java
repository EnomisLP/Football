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
import com.example.demo.projections.PlayersNodeDTO;
import com.example.demo.projections.UsersNodeProjection;
import com.example.demo.services.Neo4j.Users_node_service;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/v1/")

public class Users_node_controller {

    @Autowired
    private final Users_node_service Uns;
    private final AuthenticationManager authenticationManager;

    public Users_node_controller(Users_node_service uns, AuthenticationManager a) {
        this.Uns = uns;
        this.authenticationManager = a;
    }

    // READ
    
    /*// DISABLED
    @GetMapping("Users_Node/user/{username}")
    @Operation(summary = "READ operation: get users_nodes", tags={"Clarify"})
    public UsersNode getUser(@PathVariable String username) {
        return Uns.getUsers(username);
    }*/

    //???????
    @GetMapping("admin/user/node/list")
    @Operation(summary = "READ: get all Users_node", tags={"Admin:User"})
    public Page<UsersNode> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return Uns.getAllUsers(pageable);
    }

    @GetMapping("user/{userName}/articles")
    @Operation(summary = "READ: get all articles of a user", tags={"User"})
    public Page<ArticlesNode> getUserArticles(@PathVariable String userName,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return Uns.getUserArticles(userName, pageable);
    }

    /* DISABLED
    @GetMapping("Users_Node/user/articles/{articleId}")
    @Operation(summary = "READ: get a specific article of a user", tags={"Clarify"})
    public ArticlesNode getUserArticle(Authentication auth, @PathVariable String articleId) {
        return Uns.getSpecificUserArticle(auth.getName(), articleId);
    }
    */
    
    
    @GetMapping("user/team/male/getStats")
    @Operation(summary="Retrieve the stats of the players inside the personal team (Male)", tags={"User"})
    public List<FifaStatsPlayer> getFifaMStats(Authentication auth) {
        return Uns.ShowUserMPlayersStats(auth.getName());
    }

    @GetMapping("user/team/female/getStats")
    @Operation(summary="Retrieve the stats of the players inside the personal team (Female)", tags={"User"})
    public List<FifaStatsPlayer> getFifaFStats(Authentication auth) {
        return Uns.ShowUserFPlayersStats(auth.getName());
    }

    @GetMapping("user/team/male")
    @Operation(summary = "Get list of players inside the male team", tags={"User"})
    public List<PlayersNodeDTO> geMPlayers(Authentication auth) {
        return Uns.ShowUserMPlayers(auth.getName());
    }

    @GetMapping("user/team/female")
    @Operation(summary = "Get list of players inside the female team", tags={"User"})
    public List<PlayersNodeDTO> getFPlayers(Authentication auth) {
        return Uns.ShowUserFPlayers(auth.getName());
    }

    @GetMapping("user/{userName}/followings")
    @Operation(summary = "Get list of users followed by a user", tags={"User"})
    public List<UsersNodeProjection> getFollowings(@PathVariable String userName) {
        return Uns.getFollowings(userName);
    }

    @GetMapping("user/{userName}/followers")
    @Operation(summary = "Get list of followers of an user", tags={"User"})
    public List<UsersNodeProjection> getFollowedBy(@PathVariable String userName) {
        return Uns.getFollowedBy(userName);
    }

    // MAP USERS
    @PostMapping("admin/map/users")
    @Operation(summary = "Map all the Users from MongoDB to Neo4j", tags={"Admin:Map"})
    public String MapAllUsers() {
        return Uns.mapAllUsersToNeo4j();
    }
    
    @PutMapping("admin/populate/likes_to_players")
    @Operation(summary = "Populate neo4j with likes to players", tags={"Admin:Populate"})
    public String populateLikesToPlayer() {
        return Uns.populateLikesToPlayer();
    }
    
    @PutMapping("admin/populate/likes_to_teams")
    @Operation(summary = "Populate neo4j with likes to teams", tags={"Admin:Populate"})
    public String populateLikesToTeams() {
        return Uns.populateLikesToTeams();
    }
    
    @PutMapping("admin/populate/likes_to_coaches")
    @Operation(summary = "Populate neo4j with likes to coaches", tags={"Admin:Populate"})
    public String populateLikesToCoaches() {
        return Uns.populateLikesToCoaches();
    }
    
    @PutMapping("admin/populate/follows")
    @Operation(summary = "Populate neo4j with follows to user", tags={"Admin:Populate"})
    public String populateFollowsToUsers() {
        return Uns.populateFollowsToUsers();
    }

    // DELETE PLAYERS
     @DeleteMapping("user/team/male/removePlayer/{_id}")
    @Operation(summary = "Remove a player in male Team by its mongoId", tags={"User"})
    public CompletableFuture<String> removeMPlayer(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
        return Uns.removePlayerMTeam(auth.getName(), _id);
    }

   @DeleteMapping("user/team/female/removePlayer/{_id}")
    @Operation(summary = "Remove a player in female Team by its mongoId", tags={"User"})
    public CompletableFuture<String> removeFPlayer(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
        return Uns.removePlayerFTeam(auth.getName(), _id);
    }

    // FOLLOW / UNFOLLOW
    @PutMapping("user/{target}/follow")
    @Operation(summary = "Follow a user", tags={"User"})
    public CompletableFuture<String> FOLLOW(Authentication auth, @PathVariable String target) throws JsonProcessingException {
        return Uns.FOLLOW(auth.getName(), target);
    }

    @DeleteMapping("user/{target}/unfollow")
    @Operation(summary = "Remove follow from a user", tags={"User"})
    public CompletableFuture<String> UNFOLLOW(Authentication auth, @PathVariable String target) throws JsonProcessingException {
        return Uns.UNFOLLOW(auth.getName(), target);
    }

    //LIKE / UNLIKE ARTICLES
     @PostMapping("/user/article/{articleId}/like")
    @Operation(summary = "LIKE an article by its mongoId", tags={"Article"})
    public CompletableFuture<String> articleLIKE(@PathVariable String articleId, Authentication auth) throws JsonProcessingException {
        return Uns.LIKE_ARTICLE(auth.getName(), articleId);
    }

     @DeleteMapping("/user/article/{articleId}/unlike")
    @Operation(summary = "UNLIKE an article by its mongoId", tags={"Article"})
    public CompletableFuture<String> articleUNLIKE(@PathVariable String articleId, Authentication auth) throws JsonProcessingException {
        return Uns.UNLIKE_ARTICLE(auth.getName(), articleId);
    }
    
    // ADD TO TEAM
   @PostMapping("user/team/male/addPlayer/{_id}/{fifaValue}")
    @Operation(summary = "ADD a player in male Team by its mongoId", tags={"Player"})
    public CompletableFuture<String> add_in_M_Team(@PathVariable String _id, @PathVariable int fifaValue, Authentication auth) throws JsonProcessingException {
        return Uns.addInMTeam(auth.getName(), _id, fifaValue);
    }

    @PostMapping("user/team/female/addPlayer/{_id}/{fifaValue}")
    @Operation(summary = "ADD a player in Female Team by its mongoId", tags={"Player"})
    public CompletableFuture<String> add_in_F_Team(@PathVariable String _id, @PathVariable int fifaValue, Authentication auth) throws JsonProcessingException {
        return Uns.addInFTeam(auth.getName(), _id, fifaValue);
    }

    // LIKES
    @PostMapping("/user/team/{_id}/like")
    @Operation(summary = "LIKE a team by its mongoId", tags={"Team"})
    public CompletableFuture<String> teamLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
        return Uns.team_LIKE(auth.getName(), _id);
    }

       @DeleteMapping("/user/team/{_id}/unlike")
    @Operation(summary = "UNLIKE a team by its mongoId", tags={"Team"})
    public CompletableFuture<String> teamUNLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
        return Uns.team_UNLIKE(auth.getName(), _id);
    }

     @PostMapping("/user/player/{_id}/like")
    @Operation(summary = "LIKE a player by its mongoId", tags={"Player"})
    public CompletableFuture<String> playerLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
        return Uns.player_LIKE(auth.getName(), _id);
    }

    @DeleteMapping("/user/player/{_id}/unlike")
    @Operation(summary = "UNLIKE a player by its mongoId", tags={"Player"})
    public CompletableFuture<String> playerUNLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
        return Uns.player_UNLIKE(auth.getName(), _id);
    }

   @PostMapping("/user/coach/{_id}/like")
    @Operation(summary = "LIKE a coach by its mongoId", tags={"Coach"})
    public CompletableFuture<String> coachLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
        return Uns.coach_LIKE(auth.getName(), _id);
    }

     @DeleteMapping("/user/coach/{_id}/unlike")
    @Operation(summary = "UNLIKE a coach by its mongoId", tags={"Coach"})
    public CompletableFuture<String> coachUNLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
        return Uns.coach_UNLIKE(auth.getName(), _id);
    }
    
}
