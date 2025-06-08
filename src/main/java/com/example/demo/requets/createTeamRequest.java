package com.example.demo.requets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class createTeamRequest {
    
    private String team_name;
    private String league_name;
    private Integer league_level;
    private String nationality_name;
    private String gender;
}
