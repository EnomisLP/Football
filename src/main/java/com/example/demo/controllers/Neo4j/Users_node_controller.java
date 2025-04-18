package com.example.demo.controllers.Neo4j;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.projections.UsersNodeProjection;
import com.example.demo.relationships.has_in_F_team;
import com.example.demo.relationships.has_in_M_team;
import com.example.demo.requets.AddPlayerToTeamRequest;
import com.example.demo.requets.FollowRequest;
import com.example.demo.requets.LikeRequest;
import com.example.demo.services.Neo4j.Users_node_service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public <Pageable> Page<UsersNode> getAllUsers(@RequestParam(defaultValue = "0") int page,
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
    @PutMapping("/user/follow")
    public void FOLLOW(@RequestBody FollowRequest request) {
        Uns.FOLLOW(request.getFollower(), request.getFollowee());
    }

    @DeleteMapping("/user/unfollow")
    public void UNFOLLOW(@RequestBody FollowRequest request) {
        Uns.UNFOLLOW(request.getFollower(), request.getFollowee());
    }

    // ADD TO TEAM
    @PostMapping("/user/MaleTeam")
    public String add_in_M_Team(@RequestBody AddPlayerToTeamRequest request, Authentication auth) {
        return Uns.addInMTeam(auth.getName(), request.getPlayerId(), request.getFifaValue());
    }

    @PostMapping("/user/FemaleTeam")
    public String add_in_F_Team(@RequestBody AddPlayerToTeamRequest request, Authentication auth) {
        return Uns.addInFTeam(auth.getName(), request.getPlayerId(), request.getFifaValue());
    }

    // LIKES
    @PostMapping("/user/like/team")
    public String teamLIKE(@RequestBody LikeRequest request, Authentication auth) {
        return Uns.team_LIKE(auth.getName(), request.getTargetId());
    }

    @DeleteMapping("/user/unlike/team")
    public String teamUNLIKE(@RequestBody LikeRequest request, Authentication auth) {
        return Uns.team_UNLIKE(auth.getName(), request.getTargetId());
    }

    @PostMapping("/user/like/coach")
    public String coachLIKE(@RequestBody LikeRequest request, Authentication auth) {
        return Uns.coach_LIKE(auth.getName(), request.getTargetId().intValue());
    }

    @DeleteMapping("/user/unlike/coach")
    public String coachUNLIKE(@RequestBody LikeRequest request, Authentication auth) {
        return Uns.coach_UNLIKE(auth.getName(), request.getTargetId().intValue());
    }
}
