package com.example.demo.aggregations.DTO;

import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.models.Neo4j.TeamsNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class TopTeam {
    private TeamsNodeDTO teamNode;
    private Integer totalFame;
}


