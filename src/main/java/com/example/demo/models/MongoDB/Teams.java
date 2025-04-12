package com.example.demo.models.MongoDB;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
@Document(collection ="Teams")

public class Teams {
    public Teams() {
        
    }

    @Id
    @GeneratedValue
    private String _id;
    @Field("team_id")
    private Long team_id;
    @Field("team_name")
    private String team_name;

    private List<FifaStatsTeam> fifaStats;
    
    @Field("gender")
    private String gender;
    //Constructor, Getters, Setters automatically generated
}
