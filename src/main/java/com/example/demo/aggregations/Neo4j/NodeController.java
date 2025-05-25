package com.example.demo.aggregations.Neo4j;

import java.util.Collection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.aggregations.DTO.MostEngaggedPlayer;
import com.example.demo.aggregations.DTO.TopTeam;
import com.example.demo.aggregations.DTO.UserInterestDiversity;
import io.swagger.v3.oas.annotations.Operation;

@RestController

@RequestMapping("/api/v1/")


public class NodeController {

    private final NodeService teamsNodeService;

    public NodeController(NodeService service) {
        this.teamsNodeService = service;
    }

    @GetMapping("admin/analytics/team/topByFame/{fifaVersion}")
    @Operation(summary = "Get the most famous team by FIFA version (Neo4j)", tags={"Aggregation"})
    public Collection<TopTeam> getTopFamousTeam(@PathVariable Integer fifaVersion) {
        return teamsNodeService.findMostFamousTeam(fifaVersion);
    }

    @GetMapping("admin/user/analytics/player/mostEngaged")
    @Operation(summary = "Get the most engaged player", tags={"Aggregation"})
    public Collection<MostEngaggedPlayer> getMostEngagedPlayer() {
        return teamsNodeService.findMostEngagedPlayer();
    }
    @GetMapping("admin/analytics/user/interestDiversity")
    @Operation(summary = "Get user interest diversity", tags={"Aggregation"})
    public Collection<UserInterestDiversity> getUserInterestDiversity() {
        return teamsNodeService.findUserInterestDiversity();
    }
}