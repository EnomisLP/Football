package com.example.demo.controllers.Neo4j;

import java.util.List;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.example.demo.DTO.ArticlesNodeDTO;
import com.example.demo.DTO.PlayersNodeDTO;
import com.example.demo.DTO.UsersNodeDTO;
import com.example.demo.DTO.UsersNodeProjection;
import com.example.demo.DTO.ffCountDTO;
import com.example.demo.models.MongoDB.FifaStatsPlayer;
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
    public Page<UsersNodeDTO> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return Uns.getAllUsers(pageable);
    }

    @GetMapping("user/{userName}/articles")
    @Operation(summary = "READ: get all articles of a user", tags={"User:Article"})
    public Page<ArticlesNodeDTO> getUserArticles(@PathVariable String userName,
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
    
   /*  @PutMapping("admin/populate/likes_to_players")
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
*/
    // DELETE PLAYERS
     @DeleteMapping("user/team/male/removePlayer/{_id}")
    @Operation(summary = "Remove a player in male Team by its mongoId", tags={"Player"})
    public ResponseEntity<Void> removeMPlayer(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
         Uns.removePlayerMTeam(auth.getName(), _id);
         return ResponseEntity.accepted().build();
    }

   @DeleteMapping("user/team/female/removePlayer/{_id}")
    @Operation(summary = "Remove a player in female Team by its mongoId", tags={"Player"})
    public ResponseEntity<Void> removeFPlayer(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
        Uns.removePlayerFTeam(auth.getName(), _id);
        return ResponseEntity.accepted().build();
    }

    // FOLLOW / UNFOLLOW
    
    @GetMapping("user/{userName}/followersAndFollowingsCounts")
    @Operation(summary = "Get the number of followers and followings of a user", tags={"User"})
    public ResponseEntity<ffCountDTO> getFFCount(@PathVariable String userName) throws JsonProcessingException {
         ffCountDTO dto = Uns.countFollowersAndFollowings(userName);
         if (dto.getFollowersCount() == -1 && dto.getFollowingsCount() == -1) {
                // Return 404 Not Found
                return ResponseEntity.notFound().build();
            } else {
                // Return 200 OK with the DTO
                return ResponseEntity.ok(dto);
            }
    }
    
    @PutMapping("user/{userName}/follow")
    @Operation(summary = "Follow a user", tags={"User"})
    public ResponseEntity<Void> FOLLOW(Authentication auth, @PathVariable String userName) throws JsonProcessingException {
        Uns.FOLLOW(auth.getName(), userName);
         return ResponseEntity.accepted().build();
    }

    @DeleteMapping("user/{userName}/unfollow")
    @Operation(summary = "Remove follow from a user", tags={"User"})
    public ResponseEntity<Void> UNFOLLOW(Authentication auth, @PathVariable String userName) throws JsonProcessingException {
         Uns.UNFOLLOW(auth.getName(), userName);
         return ResponseEntity.accepted().build();
    }

    //LIKE / UNLIKE ARTICLES
     @PostMapping("article/{articleId}/like")
    @Operation(summary = "LIKE an article by its mongoId", tags={"Article"})
    public ResponseEntity<Void> articleLIKE(@PathVariable String articleId, Authentication auth) throws JsonProcessingException {
         Uns.LIKE_ARTICLE(auth.getName(), articleId);
         return ResponseEntity.accepted().build();
    }

     @DeleteMapping("article/{articleId}/unlike")
    @Operation(summary = "UNLIKE an article by its mongoId", tags={"Article"})
    public ResponseEntity<Void> articleUNLIKE(@PathVariable String articleId, Authentication auth) throws JsonProcessingException {
         Uns.UNLIKE_ARTICLE(auth.getName(), articleId);
         return ResponseEntity.accepted().build();
    }
    
    // ADD TO TEAM
   @PostMapping("user/team/male/addPlayer/{_id}/{fifaValue}")
    @Operation(summary = "ADD a player in male Team by its mongoId", tags={"Player"})
    public ResponseEntity<Void> add_in_M_Team(@PathVariable String _id, @PathVariable int fifaValue, Authentication auth) throws JsonProcessingException {
         Uns.addInMTeam(auth.getName(), _id, fifaValue);
         return ResponseEntity.accepted().build();
    }

    @PostMapping("user/team/female/addPlayer/{_id}/{fifaValue}")
    @Operation(summary = "ADD a player in Female Team by its mongoId", tags={"Player"})
    public ResponseEntity<Void> add_in_F_Team(@PathVariable String _id, @PathVariable int fifaValue, Authentication auth) throws JsonProcessingException {
        Uns.addInFTeam(auth.getName(), _id, fifaValue);
        return ResponseEntity.accepted().build();
    }

    // LIKES
    @PostMapping("team/{_id}/like")
    @Operation(summary = "LIKE a team by its mongoId", tags={"Team"})
    public ResponseEntity<Void> teamLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
         Uns.team_LIKE(auth.getName(), _id);
         return ResponseEntity.accepted().build();
    }

       @DeleteMapping("team/{_id}/unlike")
    @Operation(summary = "UNLIKE a team by its mongoId", tags={"Team"})
    public ResponseEntity<Void> teamUNLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
         Uns.team_UNLIKE(auth.getName(), _id);
         return ResponseEntity.accepted().build();
    }

     @PostMapping("player/{_id}/like")
    @Operation(summary = "LIKE a player by its mongoId", tags={"Player"})
    public ResponseEntity<Void> playerLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
         Uns.player_LIKE(auth.getName(), _id);
         return ResponseEntity.accepted().build();
    }

    @DeleteMapping("player/{_id}/unlike")
    @Operation(summary = "UNLIKE a player by its mongoId", tags={"Player"})
    public ResponseEntity<Void> playerUNLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
         Uns.player_UNLIKE(auth.getName(), _id);
         return ResponseEntity.accepted().build();
    }

   @PostMapping("coach/{_id}/like")
    @Operation(summary = "LIKE a coach by its mongoId", tags={"Coach"})
    public ResponseEntity<Void> coachLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
         Uns.coach_LIKE(auth.getName(), _id);
         return ResponseEntity.accepted().build();
    }

     @DeleteMapping("coach/{_id}/unlike")
    @Operation(summary = "UNLIKE a coach by its mongoId", tags={"Coach"})
    public ResponseEntity<Void> coachUNLIKE(@PathVariable String _id, Authentication auth) throws JsonProcessingException {
         Uns.coach_UNLIKE(auth.getName(), _id);
         return ResponseEntity.accepted().build();
    }
    
    @GetMapping("user/{_id}/check_follow")
    @Operation(summary = "Check if the user alredy follow the target user", tags={"User"})
    public ResponseEntity<Boolean> checkFollows(Authentication auth, @PathVariable String _id) {
        return ResponseEntity.ok(this.Uns.checkFollows(auth.getName(), _id));
    }
    
}
