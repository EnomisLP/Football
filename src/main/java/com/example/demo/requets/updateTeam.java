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
public class updateTeam {
    private String gender;
    private String team_name;
    
    private String league_name;
    private Integer league_level;
   
    private String nationality_name;
}
