package com.example.demo.aggregations.Neo4j;

import java.util.Collection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.aggregations.DTO.TopTeamProjection;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/Neo4jAggregations")
public class NodeController {

    private final NodeService teamsNodeService;

    public NodeController(NodeService service) {
        this.teamsNodeService = service;
    }

    @GetMapping("/top-by-fame/{fifaVersion}")
    @Operation(summary = "Get the most famous team by FIFA version")
    public Collection<TopTeamProjection> getTopFamousTeam(@PathVariable Integer fifaVersion) {
        return teamsNodeService.findMostFamousTeam(fifaVersion);
    }
}
