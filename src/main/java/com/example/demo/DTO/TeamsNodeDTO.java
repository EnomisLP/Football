package com.example.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamsNodeDTO {
    private String mongoId;
    private String longName;
    private String gender;
    private Integer fifaVersion;
}
