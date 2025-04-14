package com.example.demo.aggregations.MongoDB.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class DreamTeamPlayer {
    private String playerName;
    private int overall;
    private int fifaVersion;
    private String position;
}