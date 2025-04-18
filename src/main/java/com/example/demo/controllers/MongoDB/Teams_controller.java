package com.example.demo.controllers.MongoDB;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.services.MongoDB.Teams_service;
import com.example.demo.requets.updateTeam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/Teams")
@Tag(name = "Teams", description = "QUERIES AND AGGREGATION FOR TEAMS")
public class Teams_controller {

    private final Teams_service teamsMService;

    public Teams_controller(Teams_service teamsMService) {
        this.teamsMService = teamsMService;
    }

    // READ: Get team by ID
    @GetMapping("/{_id}")
    @Operation(summary = "READ: Get a team by its ID")
    public Teams getTeam(@PathVariable String _id) {
        return teamsMService.getTeam(_id);
    }

    // READ: Get all teams by gender with pagination
    @GetMapping("/admin/byGender/{gender}")
    @Operation(summary = "READ: Get all teams for a specific gender with pagination")
    public Page<Teams> getAllTeams(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int size, @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return teamsMService.getAllTeams(pageable, gender);
    }

    // READ: Get current formation of a team
    @GetMapping("/{teamId}/formation")
    @Operation(summary = "READ: Show the current formation of the team")
    public List<Players> showCurrentFormation(@PathVariable Long teamId) {
        return teamsMService.showCurrentFormation(teamId);
    }

    // READ: Get specific formation of a team for a particular FIFA version
    @GetMapping("/{teamId}/formation/{fifaV}")
    @Operation(summary = "READ: Show a specific formation for a given FIFA version")
    public List<Players> showSpecificFormation(@PathVariable Long teamId, @PathVariable Integer fifaV) {
        return teamsMService.showSpecificFormation(teamId, fifaV);
    }

    // READ: Get current coach of the team
    @GetMapping("/{coachId}")
    @Operation(summary = "READ: Show the current coach of the team")
    public Coaches showCurrentCoach(@PathVariable Long coachId) {
        return teamsMService.showCurrentCoach(coachId);
    }

    // READ: Get a specific coach for a given FIFA version
    @GetMapping("/{coachId}/coach/{fifaV}")
    @Operation(summary = "READ: Show a specific coach for a given FIFA version")
    public Coaches showSpecificCoach(@PathVariable Long coachId, @PathVariable Integer fifaV) {
        return teamsMService.showSpecificCoach(coachId, fifaV);
    }

    // CREATE: Create a new team
    @PostMapping("/admin")
    @Operation(summary = "CREATE: Add a new team")
    public Teams createTeam(@RequestBody Teams team) {
        return teamsMService.createTeam(team);
    }

    // UPDATE: Update an existing team
    @PutMapping("/admin/{_id}")
    @Operation(summary = "UPDATE: Update an existing team by its ID")
    public Teams updateTeam(@PathVariable String _id, @RequestBody updateTeam teamDetails) {
        return teamsMService.updateTeam(_id, teamDetails);
    }

    // DELETE: Delete a team by ID
    @DeleteMapping("/admin/{_id}")
    @Operation(summary = "DELETE: Delete a team by its ID")
    public ResponseEntity<Void> deleteTeam(@PathVariable String _id) {
        teamsMService.deleteTeam(_id);
        return ResponseEntity.noContent().build();
    }
}
