package com.example.demo.aggregations.DTO;

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
    private Integer overall;
    private String position;
}