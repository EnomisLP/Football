package com.example.demo.models.Neo4j;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Node(labels = "TeamsNode")

@Getter
@Setter
@AllArgsConstructor
@Data
public class TeamsNode {
    public TeamsNode() {
        
    }
    @Id
    @GeneratedValue
    private Long _id;
    @Property(name ="mongoId")
    @Indexed(unique = true)
    private String mongoId;
    @Property(name = "teamId")
    @Indexed(unique = true)
    private Long teamId;
    @Property(name ="teamName")
    private String teamName;

    @Property(name="gender")
    @Indexed
    private String gender;

}

