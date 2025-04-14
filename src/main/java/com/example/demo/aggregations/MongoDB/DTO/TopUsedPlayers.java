package com.example.demo.aggregations.MongoDB.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopUsedPlayers {

    private String long_name;

    private Integer usage;
    private Double maxOverall;
    private Double maxValue;
    private Integer fifaVersion;
}
