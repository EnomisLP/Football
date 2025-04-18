package com.example.demo.models.MongoDB;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

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
@Document(collection = "Coaches")
public class Coaches{
    @Id
    @GeneratedValue
    private String _id;
    @Field("coach_id")
    private Integer coach_id;
    @Field("long_name")
    private String long_name;
    @Field("short_name")
    private String short_name;
    @Field("nationality_name")
    private String nationality_name;
    @Field("gender")
    private String gender;
    //Constructors, Getters and Setters automatically Generated
}