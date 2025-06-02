package com.example.demo.controllers.Neo4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.example.demo.DTO.PlayersNodeDTO;
import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.services.Neo4j.Players_node_service;
import io.swagger.v3.oas.annotations.Operation;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

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
    @Operation(summary = "READ operation: Get a Player node by ID", tags={"Admin:Player"})
    public PlayersNodeDTO getPlayerById(@PathVariable String _id) {
        return playersNodeService.getPlayers(_id);
    }

    // READ: Get all players by gender
    @GetMapping("search/filter/player/list/byGender/{gender}")
    @Operation(summary = "READ: Get all players for a specific gender with pagination", tags = {"Player"})
    public Page<PlayersNodeDTO> getAllPlayers(
        @PathVariable String gender,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size) {

    Pageable pageable = PageRequest.of(page, size);
    return playersNodeService.getAllPlayers(gender, pageable);
}


    // READ: Show the current team a player plays in
    @GetMapping("player/{_id}/team")
    @Operation(summary = "READ: Show the team a player is currently playing in", tags={"Player"})
    public TeamsNodeDTO showCurrentTeam(@PathVariable String _id) {
        return playersNodeService.showCurrentTeam(_id);
    }

    // READ: Show a specific team a player played in with a certain FIFA version
    @GetMapping("player/{_id}/team/{fifaV}")
    @Operation(summary = "READ: Show the team in which a player played in specific year", tags={"Player"})
    public TeamsNodeDTO showSpecificTeam(@PathVariable String _id, @PathVariable Integer fifaV) {
        return playersNodeService.showSpecificTeam(_id, fifaV);
    }

    // MAP: Map all PLAYS_IN_TEAM relationships by gender
    @PostMapping("admin/map/plays_relationships/{gender}")
    @Operation(summary = "MAP: Create all PLAYS_IN_TEAM relationships from MongoDB data for a given gender", tags={"Admin:Map"})
    public String mapAllPlaysInTeam(@PathVariable String gender) {
        return playersNodeService.MapAllPlaysInTeamRel(gender);
    }

    // MAP: Map all players to Neo4j
    @PostMapping("admin/map/players")
    @Operation(summary = "MAP: Map all players from MongoDB to Neo4j nodes", tags={"Admin:Map"})
    public String mapAllPlayersToNeo4j() {
        return playersNodeService.MapAllTheNodes();
    }
    
    @GetMapping("player/{_id}/check_like")
    @Operation(summary = "Check if the user alredy liked the player", tags={"Player"})
    public ResponseEntity<Boolean> checkLike(@PathVariable String _id,Authentication auth) {
        return ResponseEntity.ok(this.playersNodeService.checkLike(_id,auth.getName()));
    }
     @GetMapping("player/{_id}/count_like")
    @Operation(summary = "Counts number of likes", tags={"Player"})
    public ResponseEntity<Integer> countLike(@PathVariable String _id) {
        return ResponseEntity.ok(this.playersNodeService.countLike(_id));
    }
    
}