package com.example.demo.controllers.Neo4j;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.example.demo.DTO.CoachesNodeDTO;
import com.example.demo.DTO.PlayersNodeDTO;
import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.services.Neo4j.Teams_node_service;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/")


public class Teams_node_controller {

    @Autowired
    private Teams_node_service teamsNodeService;

    public Teams_node_controller(Teams_node_service teamsNodeService) {
        this.teamsNodeService = teamsNodeService;
    }

    // READ: Get one team node by ID
    @GetMapping("admin/teamNode/{_id}")
    @Operation(summary = "READ operation: Get a Team node by ID", tags={"Admin:Team"})
    public TeamsNodeDTO getTeamById(@PathVariable String _id) {
        return teamsNodeService.getTeams(_id);
    }

    // READ: Get all teams by gender
    @GetMapping("search/filter/team/list/byGender/{gender}")
    @Operation(summary = "READ: Get all teams for a specific gender with pagination", tags={"Team"})
    public Page<TeamsNodeDTO> getAllTeams(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int size, @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return teamsNodeService.getAllTeams(gender, pageable);
    }

    // MAP: Map all teams from MongoDB to Neo4j
    @PostMapping("admin/map/teams")
    @Operation(summary = "MAP operation: Map all Teams from MongoDB to Neo4j", tags={"Admin:Map"})
    public String mapAllNodes() {
        return teamsNodeService.MapAllTheNodes();
    }
    
    @GetMapping("team/{_id}/formation")
    @Operation(summary = "Show formation of a Team for the current Year", tags={"Team"})
    public List<PlayersNodeDTO> showCurrentFormation(@PathVariable String _id){
        return teamsNodeService.showCurrentFormation(_id);
    }

    @GetMapping("team/{_id}/formation/{fifaV}")
    @Operation(summary = "Show formation of a Team for a specific Year", tags={"Team"})
    public List<PlayersNodeDTO> showSpecificFormation(@PathVariable String _id, @PathVariable Integer fifaV){
        return teamsNodeService.showSpecificFormation(_id, fifaV);
    }

    @GetMapping("team/{_id}/coach")
    @Operation(summary = "Show Coach of a Team for the current Year", tags={"Team"})
    public CoachesNodeDTO showCurrentCoach(@PathVariable String _id){
        return teamsNodeService.showCurrentCoach(_id);
    }

    @GetMapping("team/{_id}/coach/{fifaV}")
    @Operation(summary = "Show coach of a Team for a specific Year", tags={"Team"})
    public CoachesNodeDTO showSpecificCoach(@PathVariable String _id, @PathVariable Integer fifaV){
        return teamsNodeService.showSpecificCoach(_id, fifaV);
    }
    
    @GetMapping("team/{_id}/check_like")
    @Operation(summary = "Check if the user alredy liked the team", tags={"Team"})
    public ResponseEntity<Boolean> checkLike(@PathVariable String _id,Authentication auth) {
        return ResponseEntity.ok(this.teamsNodeService.checkLike(_id,auth.getName()));
    }

    @GetMapping("team/{_id}/count_like")
    @Operation(summary = "Counts number of likes", tags={"Team"})
    public ResponseEntity<Integer> countLike(@PathVariable String _id) {
        return ResponseEntity.ok(this.teamsNodeService.countLike(_id));
    }
}