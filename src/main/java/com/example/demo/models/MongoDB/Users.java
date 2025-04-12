package com.example.demo.models.MongoDB;



import java.util.List;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    
    @Id
    @GeneratedValue

    private String _id;
    private String username;
    private String password;
    private List<ROLES> roles;
    private List<Articles> articles;

}
