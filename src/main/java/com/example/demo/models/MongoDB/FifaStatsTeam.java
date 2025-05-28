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
    private Long club_worth_eur;
    private Integer attack;
    private Integer defence;
    private Integer midfield;
    private CoachObj coach;
    //Constructor, Getters and Setters automatically generated
}
