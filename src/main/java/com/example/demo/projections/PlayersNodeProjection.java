package com.example.demo.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayersNodeProjection {

    public String longName;
    public String gender;
    public String mongoId;
    public String nationalityName;
    public Integer age;
    public Integer fifaV;


}
