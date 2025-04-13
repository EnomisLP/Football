package com.example.demo.aggregations.MongoDB.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TopPlayersByCoach {
    private String playerName;
    private Integer overall;
    private Integer coachId;
    private Integer fifaVersion;

    // Constructors, Getters, Setters
}
