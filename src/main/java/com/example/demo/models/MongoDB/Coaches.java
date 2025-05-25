package com.example.demo.models.MongoDB;

import java.util.ArrayList;
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
@Data
@AllArgsConstructor
@Document(collection = "Coaches")
public class Coaches{
    @Id
    @GeneratedValue
    private String _id;
    @Field("long_name")
    private String long_name;
    @Field("short_name")
    private String short_name;
    @Field("nationality_name")
    private String nationality_name;
    @Field("gender")
    @Indexed
    private String gender;
    private List<TeamObj> team;
    //Constructors, Getters and Setters automatically Generated

    public Coaches() {
    this.team = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
        TeamObj teamObj = new TeamObj();
        teamObj.setFifa_version(-1);
        teamObj.setTeam_mongo_id("XXXXXXXXXXXX");
        teamObj.setTeam_name("DefaultTeamName");
        this.team.add(teamObj);
    }
}

}