package com.example.demo.models.MongoDB;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
    @Field("team_name")
    private String team_name;

    private List<FifaStatsTeam> fifaStats;
    @Field("league_id")
    private Long league_id;
    @Field("league_name")
    private String league_name;
    @Field("league_level")
    private Integer league_level;
    @Field("nationality_id")
    private Long nationality_id;
    @Field("nationality_name")
    private String nationality_name;
    @Field("gender")
    @Indexed
    private String gender;
    //Constructor, Getters, Setters automatically generated
}
