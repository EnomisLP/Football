package com.example.demo.aggregations.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public  class ClubAverage {
    private String clubName;
    private Double averageOverall;
    private Long maxClubWorth;
}