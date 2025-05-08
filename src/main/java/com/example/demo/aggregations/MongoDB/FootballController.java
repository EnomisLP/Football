package com.example.demo.aggregations.MongoDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.aggregations.DTO.ClubAverage;
import com.example.demo.aggregations.DTO.DreamTeamPlayer;
import com.example.demo.aggregations.DTO.TopPlayersByCoach;

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

   /*  @GetMapping("/top-clubs")
    @Operation(summary = "Get top clubs by average overall rating")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved top clubs"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<ClubAverage> getTopClubs() {
        return footballService.getTopClubsByAverageOverall();
    }
    */
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

