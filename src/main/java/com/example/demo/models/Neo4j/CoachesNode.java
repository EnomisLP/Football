package com.example.demo.models.Neo4j;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.example.demo.relationships.manages_team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Data
@AllArgsConstructor
@Node(labels = "CoachesNode")
public class CoachesNode {

    public CoachesNode() {
        // Default constructor
    }

    @Id
    @GeneratedValue
    private Long _id;

    @Property(name = "mongoId")
    private String mongoId;

    @Property(name = "coachId")
    private Integer coachId;

    @Property(name = "longName")
    private String longName;

    @Property(name = "nationalityName")
    private String nationalityName;

    @Property(name = "gender")
    private String gender;

    @Relationship(type = "MANAGES_TEAM", direction = Relationship.Direction.OUTGOING)
    private List<manages_team> teamMNodes = new ArrayList<>();

   
}
