package com.example.demo.aggregations.MongoDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.aggregations.MongoDB.DTO.ClubAverage;
import com.example.demo.aggregations.MongoDB.DTO.TopPlayersByCoach;
import com.example.demo.aggregations.MongoDB.DTO.TopUsedPlayers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
public class FootballController {

    @Autowired
    private FootballService footballService;

    @GetMapping("/top-clubs")
    @Operation(summary = "Get top clubs by average overall rating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved top clubs"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<ClubAverage> getTopClubs() {
        return footballService.getTopClubsByAverageOverall();
    }

    @GetMapping("/coach/{coachId}/top-players")
    @Operation(summary = "Get top players managed by a specific coach")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved top players"),
            @ApiResponse(responseCode = "404", description = "Coach not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<TopPlayersByCoach> getTopPlayersByCoach(@PathVariable int coachId) {
    return footballService.getTopPlayersManagedByCoach(coachId);
    }   
    @GetMapping("/top-used-players")
    @Operation(summary = "Get top used players by overall and value")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved top used players"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<TopUsedPlayers> getTopPlayers() {
    return footballService.getTopPlayersByUsageOverallAndValue();
    }

}

