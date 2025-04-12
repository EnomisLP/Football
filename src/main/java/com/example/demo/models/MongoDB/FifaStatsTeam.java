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
    private Long coach_id;
    private Long league_id;
    private String league_name;
    private Integer league_level;
    private Long nationality_id;
    private String nationality_name;
    private String home_stadium;
    private Integer overall;
    private Long captain;
    private Long club_worth_eur;
    private Integer attack;
    private Integer defence;
    private Integer midfield;
    private Long short_free_kick;
    private Long long_free_kick;
    private Long right_short_free_kick;
    private Long left_short_free_kick;
    private Long penalties;
    private Long left_corner;
    private Long right_corner;
    private Integer off_players_in_box;
    private Integer off_corners;
    private Integer off_free_kicks;
    
    //Constructor, Getters and Setters automatically generated
}
