package com.example.demo.controllers.MongoDB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.services.MongoDB.Teams_service;
import com.example.demo.requets.updateCoachTeam;
import com.example.demo.requets.updateFifaTeam;
import com.example.demo.requets.updateTeam;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/")


public class Teams_controller {

    private final Teams_service teamsMService;

    public Teams_controller(Teams_service teamsMService) {
        this.teamsMService = teamsMService;
    }

    // READ: Get team by ID
    @GetMapping("/admin/team/{_id}")
    @Operation(summary = "READ: Get a team document by its ID", tags={"Team","Admin"})
    public Teams getTeam(@PathVariable String _id) {
        return teamsMService.getTeam(_id);
    }

    // READ: Get all teams by gender with pagination
    @GetMapping("admin/byGender/{gender}")
    @Operation(summary = "READ: Get all teams for a specific gender with pagination", tags={"Admin", "Team"})
    public Page<Teams> getAllTeams(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int size, @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return teamsMService.getAllTeams(pageable, gender);
    }

    // CREATE: Create a new team
    @PostMapping("admin/team/new")
    @Operation(summary = "CREATE: Add a new team", tags={"Admin","Team","Clarify"})
    public Teams createTeam(@RequestBody Teams team) {
        return teamsMService.createTeam(team);
    }

    // UPDATE: Update an existing team
    @PutMapping("admin/team/modify/{_id}")
    @Operation(summary = "UPDATE: Update an existing team by its ID", tags={"Admin","Team"})
    public Teams updateTeam(@PathVariable String _id, @RequestBody updateTeam teamDetails) {
        return teamsMService.updateTeam(_id, teamDetails);
    }

    // UPDATE: Update a fifa version of a team
    @PutMapping("admin/team/modify/{_id}/{fifaV}")
    @Operation(summary = "UPDATE: Update a FIFA version of a team", tags={"Admin","Team","Clarify"})
    public Teams updateFifaVersion(@PathVariable String _id, @PathVariable Integer fifaV, @RequestBody updateFifaTeam teamDetails) {
        return teamsMService.updateFifaTeam(_id, fifaV, teamDetails);
    }

    //UPDATE : Update a coach of a team
    @PutMapping("admin/team/modify/{_id}/{fifaV}/coach")
    @Operation(summary = "UPDATE: Update a coach of a team", tags={"Admin","Team","Clarify"})
    public Teams updateCoach(@PathVariable String _id, @PathVariable Integer fifaV, @RequestBody updateCoachTeam coachDetails) {
        return teamsMService.updateCoachTeam(_id, fifaV, coachDetails);
    }

    // DELETE: Delete a team by ID
    @DeleteMapping("admin/team/delete/{_id}")
    @Operation(summary = "DELETE: Delete a team by its ID", tags={"Admin","Team","Clarify"})
    public ResponseEntity<Void> deleteTeam(@PathVariable String _id) {
        teamsMService.deleteTeam(_id);
        return ResponseEntity.noContent().build();
    }

    // DELETE: Delete a team by ID and FIFA version
    @DeleteMapping("admin/team/delete/{_id}/{fifaV}")
    @Operation(summary = "DELETE: Delete a team by its ID and FIFA version", tags={"Admin","Team","Clarify"})
    public ResponseEntity<Void> deleteTeamByFifaVersion(@PathVariable String _id, @PathVariable Integer fifaV) {
        teamsMService.deleteFifaTeam(_id, fifaV);
        return ResponseEntity.noContent().build();
    }
}