package com.example.demo.controllers.Neo4j;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.projections.CoachesNodeProjection;
import com.example.demo.projections.PlayersNodeDTO;
import com.example.demo.services.Neo4j.Teams_node_service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/Teams_Node")
@Tag(name = "Teams_node", description = "QUERIES AND AGGREGATION FOR TEAMS_NODE")
public class Teams_node_controller {

    @Autowired
    private Teams_node_service teamsNodeService;

    public Teams_node_controller(Teams_node_service teamsNodeService) {
        this.teamsNodeService = teamsNodeService;
    }

    // READ: Get one team node by ID
    @GetMapping("/{_id}")
    @Operation(summary = "READ operation: Get a Team node by ID")
    public TeamsNode getTeamById(@PathVariable String _id) {
        return teamsNodeService.getTeams(_id);
    }

    // READ: Get all teams by gender
    @GetMapping("/admin/byGender/{gender}")
    @Operation(summary = "READ: Get all teams for a specific gender with pagination")
    public Page<TeamsNode> getAllTeams(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int size, @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return teamsNodeService.getAllTeams(gender, pageable);
    }

    // MAP: Map all teams from MongoDB to Neo4j
    @PostMapping("/admin/map-all")
    @Operation(summary = "MAP operation: Map all Teams from MongoDB to Neo4j")
    public String mapAllNodes() {
        return teamsNodeService.MapAllTheNodes();
    }

    @GetMapping("/{_id}/currentFormation")
    @Operation(summary = "Show formation of a Team for the current Year")
    public List<PlayersNodeDTO> showCurrentFormation(@PathVariable String _id){
        return teamsNodeService.showCurrentFormation(_id);
    }

    @GetMapping("/{_id}/{fifaV}/specificFormation")
    @Operation(summary = "Show formation of a Team for a specific Year")
    public List<PlayersNodeDTO> showSpecificFormation(@PathVariable String _id, @PathVariable Integer fifaV){
        return teamsNodeService.showSpecificFormation(_id, fifaV);
    }

    @GetMapping("/{_id}/currentCoach")
    @Operation(summary = "Show Coach of a Team for the current Year")
    public CoachesNodeProjection showCurrentCoach(@PathVariable String _id){
        return teamsNodeService.showCurrentCoach(_id);
    }

    @GetMapping("/{_id}/{fifaV}/specificCoach")
    @Operation(summary = "Show coach of a Team for a specific Year")
    public CoachesNodeProjection showSpecificCoach(@PathVariable String _id, @PathVariable Integer fifaV){
        return teamsNodeService.showSpecificCoach(_id, fifaV);
    }
}
