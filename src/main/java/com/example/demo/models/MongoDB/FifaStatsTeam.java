package com.example.demo.models.MongoDB;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data

public class FifaStatsTeam {
  
    private Integer fifa_version;
    private String home_stadium;
    private Integer overall;
    private Integer club_worth_eur;
    private Integer attack;
    private Integer defence;
    private Integer midfield;
    private Integer off_players_in_box;
    private Integer off_corners;
    private Integer off_free_kicks;
    private CoachObj coach;
    //Constructor, Getters and Setters automatically generated
}
