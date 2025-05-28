package com.example.demo.controllers.MongoDB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.MongoDB.Players;
import com.example.demo.services.MongoDB.Players_service;
import com.example.demo.requets.createPlayerRequest;
import com.example.demo.requets.updateFifaPlayer;
import com.example.demo.requets.updatePlayer;
import com.example.demo.requets.updateTeamPlayer;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/")

public class Players_controller {

    private final Players_service playersMService;

    
    public Players_controller(Players_service playersMService) {
        this.playersMService = playersMService;
    }

    // READ: Get player by ID
    @GetMapping("player/{_id}")
    @Operation(summary = "READ: Get player by its ID", tags={"Player"})
    public Players getPlayer(@PathVariable String _id) {
        return playersMService.getPlayer(_id);
    }

    // READ: Get all players by gender with pagination
    @GetMapping("admin/player/byGender/{gender}")
    @Operation(summary = "READ: Get all players for a specific gender with pagination", tags={"Admin:Player"})
    public Page<Players> getAllPlayers(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size, @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return playersMService.getAllPlayers(pageable, gender);
    }

    // CREATE: Create a new player
    @PostMapping("admin/player/new")
    @Operation(summary = "CREATE: Add a new player", tags={"Admin:Player"})
    public Players createPlayer(@RequestBody createPlayerRequest player) {
        return playersMService.createPlayer(player);
    }

    // UPDATE: Update an existing player
    @PutMapping("admin/player/modify/{_id}")
    @Operation(summary = "UPDATE: Update an existing player by its ID", tags={"Admin:Player"})
    public Players updatePlayer(@PathVariable String _id, @RequestBody updatePlayer playerDetails) {
        return playersMService.updatePlayer(_id, playerDetails);
    }

    // UPDATE: Update FIFA version of a player
    @PutMapping("admin/player/modify/{_id}/{fifaV}")
    @Operation(summary = "UPDATE: Update a specific FIFA version of a player by its ID", tags={"Admin:Player"})
    public Players updateFifaVersion(@PathVariable String _id, @PathVariable Integer fifaV,
                                      @RequestBody updateFifaPlayer playerDetails) {
        return playersMService.updateFifaPlayer(_id, fifaV, playerDetails);
    }

    // UPDATE: Update team of a player
    @PutMapping("admin/player/modify/{_id}/{fifaV}/team")
    @Operation(summary = "UPDATE: Update the team of a player by its ID", tags={"Admin:Player"})
    public Players updateTeam(@PathVariable String _id, @PathVariable Integer fifaV, @RequestBody updateTeamPlayer teamDetails) {
        return playersMService.updateTeamPlayer(_id, fifaV, teamDetails);
    }

    // DELETE: Delete a player by ID
    @DeleteMapping("admin/player/delete/{_id}")
    @Operation(summary = "DELETE: Delete a player by its ID", tags={"Admin:Player"})
    public ResponseEntity<Void> deletePlayer(@PathVariable String _id) {
        playersMService.deletePlayer(_id);
        return ResponseEntity.noContent().build();
    }

    //DELETE: Delete fifaVersion of a player by ID
    @DeleteMapping("admin/player/delete/{_id}/{fifaV}")
    @Operation(summary = "DELETE: Delete FifaStats Object of a player by its ID and FIFA version", tags={"Admin:Player"})
    public ResponseEntity<Void> deleteFifaVersion(@PathVariable String _id, @PathVariable Integer fifaV) {
        playersMService.deleteFifaPlayer(_id, fifaV);
        return ResponseEntity.noContent().build();
    }
    
    /* DISABLED
    // READ: Get the last year stats of the player
    @GetMapping("player/{_id}/lastYearStats")
    @Operation(summary = "READ: Get the stats of the player for the last year", tags={"Player","Clarify"})
    public FifaStatsPlayer getLastYearStats(@PathVariable String _id) {
        return playersMService.showCurrentYear(_id);
    }
    */
    // READ: Get specific year stats of the player

    /* DISABLED 
    @GetMapping("player/{_id}/stats/{fifaV}")
    @Operation(summary = "READ: Get the stats of the player for a specific FIFA version", tags={"Player","Clarify"})
    public FifaStatsPlayer getSpecificYearStats(@PathVariable String _id, @PathVariable Integer fifaV) {
        return playersMService.showSpecificStats(_id, fifaV);
    }*/
}