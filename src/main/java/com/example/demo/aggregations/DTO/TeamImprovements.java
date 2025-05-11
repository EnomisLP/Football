package com.example.demo.aggregations.DTO;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class TeamImprovements {
    
    @Field("attack_improvement")
    private Float attImp;
    
    @Field("defence_improvement")
    private Float defImp;
    
    @Field("midfield_improvement")
    private Float midImp;
}