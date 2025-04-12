package com.example.demo.controllers.Neo4j;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.Neo4j.TeamsNode;
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
    @GetMapping("/{id}")
    @Operation(summary = "READ operation: Get a Team node by ID")
    public TeamsNode getTeamById(@PathVariable Long id) {
        return teamsNodeService.getTeams(id);
    }

    // READ: Get all teams by gender
    @GetMapping("/byGender/{gender}")
    @Operation(summary = "READ operation: Get all Team nodes by gender")
    public List<TeamsNode> getAllTeamsByGender(@PathVariable String gender) {
        return teamsNodeService.getAllTeams(gender);
    }

    // MAP: Map all teams from MongoDB to Neo4j
    @PostMapping("/admin/map-all")
    @Operation(summary = "MAP operation: Map all Teams from MongoDB to Neo4j")
    public String mapAllNodes() {
        return teamsNodeService.MapAllTheNodes();
    }
}
