package com.example.demo.aggregations.MongoDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.aggregations.DTO.DreamTeamPlayer;
import com.example.demo.aggregations.DTO.monthSummary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;


@RestController
@RequestMapping("/api/v1/MongoDB Aggregations")
@Tag(name = "Football Aggregations", description = "Football Aggregations API")
public class FootballController {

    @Autowired
    private FootballService footballService;

    @GetMapping("/analytics/subscriptionYearSummary/{year}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved subscription summary"),
            @ApiResponse(responseCode = "404", description = "Summary not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Get the summary of the subscriptions for each month of a specific year")
    public List<monthSummary> getSubscriptionYearSummary(@PathVariable Integer year) {
        return footballService.getSubscriptionYearSummary(year);
    }

 

    @GetMapping("/dream-team(4-3-3)/{fifaV}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dream team players"),
            @ApiResponse(responseCode = "404", description = "FIFA version not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Operation(summary = "Get the dream team of players")
    public List<DreamTeamPlayer> getDreamTeamPlayers(@PathVariable Integer fifaV) {
        return footballService.getDreamTeam(fifaV);
    }
    
}

