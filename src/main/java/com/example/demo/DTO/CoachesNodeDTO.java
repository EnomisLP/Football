package com.example.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoachesNodeDTO {

    private String mongoId;
    private String longName;
    private Integer fifaVersion;
    private String gender;
    

}
