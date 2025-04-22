package com.example.demo.controllers.Neo4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.MongoDB.FifaStatsPlayer;
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

    // DELETE PLAYERS
    @DeleteMapping("/user/Mplayers/{playerId}")
    public void removeMPlayer(@PathVariable Long playerId, Authentication auth) {
        Uns.removePlayerMTeam(auth.getName(), playerId);
    }

    @DeleteMapping("/user/Fplayers/{playerId}")
    public void removeFPlayer(@PathVariable Long playerId, Authentication auth) {
        Uns.removePlayerFTeam(auth.getName(), playerId);
    }

    // FOLLOW / UNFOLLOW
    @PutMapping("/user/follow/{logged}/{target}")
    public void FOLLOW(@PathVariable String logged, @PathVariable String target) {
        Uns.FOLLOW(logged, target);
    }

    @DeleteMapping("/user/unfollow/{logged}/{target}")
    public void UNFOLLOW(@PathVariable String logged, @PathVariable String target) {
        Uns.UNFOLLOW(logged, target);
    }

    // ADD TO TEAM
    @PostMapping("/user/MaleTeam/{playerId}/{fifaValue}")
    public String add_in_M_Team(@PathVariable Long playerId, @PathVariable int fifaValue, Authentication auth) {
        return Uns.addInMTeam(auth.getName(), playerId, fifaValue);
    }

    @PostMapping("/user/FemaleTeam/{playerId}/{fifaValue}")
    public String add_in_F_Team(@PathVariable Long playerId, @PathVariable int fifaValue, Authentication auth) {
        return Uns.addInFTeam(auth.getName(), playerId, fifaValue);
    }

    // LIKES
    @PostMapping("/user/like/team/{targetId}")
    public String teamLIKE(@PathVariable Long targetId, Authentication auth) {
        return Uns.team_LIKE(auth.getName(), targetId);
    }

    @DeleteMapping("/user/unlike/team/{targetId}")
    public String teamUNLIKE(@PathVariable Long targetId, Authentication auth) {
        return Uns.team_UNLIKE(auth.getName(), targetId);
    }

    @PostMapping("/user/like/coach/{targetId}")
    public String coachLIKE(@PathVariable Long targetId, Authentication auth) {
        return Uns.coach_LIKE(auth.getName(), targetId.intValue());
    }

    @DeleteMapping("/user/unlike/coach/{targetId}")
    public String coachUNLIKE(@PathVariable Long targetId, Authentication auth) {
        return Uns.coach_UNLIKE(auth.getName(), targetId.intValue());
    }
}
