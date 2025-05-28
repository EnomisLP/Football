package com.example.demo.aggregations.MongoDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.aggregations.DTO.DreamTeamPlayer;
import com.example.demo.aggregations.DTO.TeamImprovements;
import com.example.demo.aggregations.DTO.monthSummary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api/v1/")

public class FootballController {

    @Autowired
    private FootballService footballService;

    @GetMapping("admin/analytics/signupsSummary/{year}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved subscription summary"),
            @ApiResponse(responseCode = "404", description = "Summary not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Get the summary of the subscriptions for each month of a specific year", tags={"Admin:Aggregation"})
    public CompletableFuture<List<monthSummary>> getSubscriptionYearSummary(@PathVariable Integer year) {
        return footballService.getSubscriptionYearSummary(year);
    }

 

    @GetMapping("user/team/dreamTeam/{fifaV}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dream team players"),
            @ApiResponse(responseCode = "404", description = "FIFA version not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Get the dream team of players", tags={"Aggregation"})
    public CompletableFuture<List<DreamTeamPlayer>> getDreamTeamPlayers(@PathVariable Integer fifaV) {
        return footballService.getDreamTeam(fifaV);
    }
    
    // Modify with team id not name
    @GetMapping("/admin/team/{team}/analytics/improvement/{year1}/{year2}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved information"),
            @ApiResponse(responseCode = "404", description = "Team not found"),
            @ApiResponse(responseCode = "500", description = "Years not available")
    })
    @Operation(summary = "Get the percentage improvements in terms of attack, defense and midfield of a team, between 2 years", tags={ "Admin:Aggregation"})
    public CompletableFuture<TeamImprovements> getSubscriptionYearSummary(@PathVariable String team,@PathVariable String year1,@PathVariable String year2) {
        return footballService.getTeamImprovements(team,year1,year2);
    }
}