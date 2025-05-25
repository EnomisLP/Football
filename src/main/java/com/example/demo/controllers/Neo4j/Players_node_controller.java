package com.example.demo.controllers.Neo4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.Neo4j.PlayersNode;
import com.example.demo.relationships.plays_in_team;
import com.example.demo.services.Neo4j.Players_node_service;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/")


public class Players_node_controller {

    @Autowired
    private Players_node_service playersNodeService;

    public Players_node_controller(Players_node_service playersNodeService) {
        this.playersNodeService = playersNodeService;
    }

    // READ: Get player by ID
    @GetMapping("admin/playerNode/{_id}")
    @Operation(summary = "READ operation: Get a Player node by ID", tags={"Admin","Player"})
    public PlayersNode getPlayerById(@PathVariable String _id) {
        return playersNodeService.getPlayers(_id);
    }

    // READ: Get all players by gender
    @GetMapping("search/filter/player/list/byGender/{gender}")
    @Operation(summary = "READ: Get all players for a specific gender with pagination", tags={"User","Player", "Anonymous"})
    public Page<PlayersNode> getAllPlayers(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size, @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return playersNodeService.getAllPlayers(gender, pageable);
    }

    // READ: Show the current team a player plays in
    @GetMapping("/user/player/{_id}/team")
    @Operation(summary = "READ: Show the team a player is currently playing in", tags={"Team","Player","User"})
    public plays_in_team showCurrentTeam(@PathVariable String _id) {
        return playersNodeService.showCurrentTeam(_id);
    }

    // READ: Show a specific team a player played in with a certain FIFA version
    @GetMapping("/user/player/{_id}/team/{fifaV}")
    @Operation(summary = "READ: Show the team in which a player played in specific year", tags={"Team","Player","User"})
    public plays_in_team showSpecificTeam(@PathVariable String _id, @PathVariable Integer fifaV) {
        return playersNodeService.showSpecificTeam(_id, fifaV);
    }

    // MAP: Map all PLAYS_IN_TEAM relationships by gender
    @PostMapping("admin/map/plays_relationships/{gender}")
    @Operation(summary = "MAP: Create all PLAYS_IN_TEAM relationships from MongoDB data for a given gender", tags={"Admin","Map"})
    public String mapAllPlaysInTeam(@PathVariable String gender) {
        return playersNodeService.MapAllPlaysInTeamRel(gender);
    }

    // MAP: Map all players to Neo4j
    @PostMapping("admin/map/players")
    @Operation(summary = "MAP: Map all players from MongoDB to Neo4j nodes", tags={"Admin","Map"})
    public String mapAllPlayersToNeo4j() {
        return playersNodeService.MapAllTheNodes();
    }
}