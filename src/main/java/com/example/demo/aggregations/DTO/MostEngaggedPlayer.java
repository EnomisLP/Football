package com.example.demo.aggregations.DTO;

import com.example.demo.models.Neo4j.PlayersNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MostEngaggedPlayer {
    private PlayersNode player;
    private Integer totalEngagement;
}
