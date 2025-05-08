package com.example.demo.models.MongoDB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data

public class FifaStatsTeam {
  
    private Integer fifa_version;
    private Long league_id;
    private String league_name;
    private Integer league_level;
    private Long nationality_id;
    private String nationality_name;
    private String home_stadium;
    private Integer overall;
    private Integer captain;
    private Long club_worth_eur;
    private Integer attack;
    private Integer defence;
    private Integer midfield;
    private Integer short_free_kick;
    private Integer long_free_kick;
    private Integer right_short_free_kick;
    private Integer left_short_free_kick;
    private Integer penalties;
    private Integer left_corner;
    private Integer right_corner;
    private Integer off_players_in_box;
    private Integer off_corners;
    private Integer off_free_kicks;
    private CoachObj coach;
    //Constructor, Getters and Setters automatically generated
}
