package com.example.demo.requets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class updateFifaTeam {
    private Integer fifa_version;
    private Long league_id;
    private String league_name;
    private Integer league_level;
    private Long nationality_id;
    private String nationality_name;
    private String home_stadium;
    private Integer overall;
    private Long club_worth_eur;
    private Integer attack;
    private Integer defence;
    private Integer midfield;
    private Integer off_players_in_box;
    private Integer off_corners;
    private Integer off_free_kicks;
}
