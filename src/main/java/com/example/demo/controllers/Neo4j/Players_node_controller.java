package com.example.demo.controllers.Neo4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.Neo4j.PlayersNode;
import com.example.demo.relationships.plays_in_team;
import com.example.demo.services.Neo4j.Players_node_service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController
@RequestMapping("/api/v1/Players_Node")
@Tag(name = "Players_node", description = "QUERIES AND AGGREGATION FOR PLAYERS_NODE")
public class Players_node_controller {

    @Autowired
    private Players_node_service playersNodeService;

    public Players_node_controller(Players_node_service playersNodeService) {
        this.playersNodeService = playersNodeService;
    }

    // READ: Get player by ID
    @GetMapping("/{_id}")
    @Operation(summary = "READ operation: Get a Player node by ID")
    public PlayersNode getPlayerById(@PathVariable String _id) {
        return playersNodeService.getPlayers(_id);
    }

    // READ: Get all players by gender
    @GetMapping("/admin/byGender/{gender}")
    @Operation(summary = "READ: Get all players for a specific gender with pagination")
    public Page<PlayersNode> getAllPlayers(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size, @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return playersNodeService.getAllPlayers(gender, pageable);
    }

    // READ: Show the current team a player plays in
    @GetMapping("/{_id}/current-team")
    @Operation(summary = "READ: Show the team a player is currently playing in")
    public plays_in_team showCurrentTeam(@PathVariable String _id) {
        return playersNodeService.showCurrentTeam(_id);
    }

    // READ: Show a specific team a player played in with a certain FIFA version
    @GetMapping("/{_id}/team/{fifaV}")
    @Operation(summary = "READ: Show a team a player played in for a specific FIFA version")
    public plays_in_team showSpecificTeam(@PathVariable String _id, @PathVariable Integer fifaV) {
        return playersNodeService.showSpecificTeam(_id, fifaV);
    }

    // MAP: Map all PLAYS_IN_TEAM relationships by gender
    @PostMapping("/admin/relationships/{gender}")
    @Operation(summary = "MAP: Create all PLAYS_IN_TEAM relationships from MongoDB data for a given gender")
    public String mapAllPlaysInTeam(@PathVariable String gender) {
        return playersNodeService.MapAllPlaysInTeamRel(gender);
    }

    // MAP: Map all players to Neo4j
    @PostMapping("/admin/map-all")
    @Operation(summary = "MAP: Map all players from MongoDB to Neo4j nodes")
    public String mapAllPlayersToNeo4j() {
        return playersNodeService.MapAllTheNodes();
    }
}
