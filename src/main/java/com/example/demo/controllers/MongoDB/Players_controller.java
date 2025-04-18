package com.example.demo.controllers.MongoDB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.services.MongoDB.Players_service;
import com.example.demo.requets.updatePlayer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/Players")
@Tag(name = "Players", description = "QUERIES AND AGGREGATION FOR PLAYERS")
public class Players_controller {

    private final Players_service playersMService;

    
    public Players_controller(Players_service playersMService) {
        this.playersMService = playersMService;
    }

    // READ: Get player by ID
    @GetMapping("/{_id}")
    @Operation(summary = "READ: Get player by its ID")
    public Players getPlayer(@PathVariable String _id) {
        return playersMService.getPlayer(_id);
    }

    // READ: Get all players by gender with pagination
    @GetMapping("/admin/byGender/{gender}")
    @Operation(summary = "READ: Get all players for a specific gender with pagination")
    public Page<Players> getAllPlayers(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size, @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return playersMService.getAllPlayers(pageable, gender);
    }

    // CREATE: Create a new player
    @PostMapping("/admin")
    @Operation(summary = "CREATE: Add a new player")
    public Players createPlayer(@RequestBody Players player) {
        return playersMService.createPlayer(player);
    }

    // UPDATE: Update an existing player
    @PutMapping("/admin/{_id}")
    @Operation(summary = "UPDATE: Update an existing player by its ID")
    public Players updatePlayer(@PathVariable String _id, @RequestBody updatePlayer playerDetails) {
        return playersMService.updatePlayer(_id, playerDetails);
    }

    // DELETE: Delete a player by ID
    @DeleteMapping("/admin/{_id}")
    @Operation(summary = "DELETE: Delete a player by its ID")
    public ResponseEntity<Void> deletePlayer(@PathVariable String _id) {
        playersMService.deletePlayer(_id);
        return ResponseEntity.noContent().build();
    }

    // READ: Get the last year stats of the player
    @GetMapping("/{playerId}/lastYearStats")
    @Operation(summary = "READ: Get the stats of the player for the last year")
    public FifaStatsPlayer getLastYearStats(@PathVariable Long playerId) {
        return playersMService.showCurrentYear(playerId);
    }

    // READ: Get specific year stats of the player
    @GetMapping("/{playerId}/stats/{fifaV}")
    @Operation(summary = "READ: Get the stats of the player for a specific FIFA version")
    public FifaStatsPlayer getSpecificYearStats(@PathVariable Long playerId, @PathVariable Integer fifaV) {
        return playersMService.showSpecificStats(playerId, fifaV);
    }
}
