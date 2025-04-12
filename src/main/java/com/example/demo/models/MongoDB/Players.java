package com.example.demo.models.MongoDB;


import java.util.Date;
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
@Document(collection ="Players")
public class Players {
    public Players() {
        
    }

    @Id
    @GeneratedValue
    private String _id;
    @Field("player_id")
    private Long player_id;
    @Field("short_name")
    private  String short_name;
    @Field("long_name")
    private String long_name;
    @Field("age")
    private Integer age;
    @Field("dob")
    private Date dob;
    @Field("nationality_id")
    private Long nationality_id;
    @Field("nationality_name")
    private String nationality_name;
    @Field("height_cm")
    private Integer height_cm;
    @Field("weight_kg")
    private Integer weight_kg;
    
    private List<FifaStatsPlayer> fifaStats;
    @Field("gender")
    private String gender;

    //Cunstructors, Getters, Setters automatically generated
}
