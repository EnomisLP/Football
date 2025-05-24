package com.example.demo.models.Neo4j;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.example.demo.relationships.manages_team;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Node(labels = "CoachesNode")
public class CoachesNode {

    @Id
    @GeneratedValue
    private Long _id;

    @Property(name = "mongoId")
    @Indexed(unique = true)
    private String mongoId;

    @Property(name = "longName")
    @Indexed
    private String longName;

    @Property(name = "nationalityName")
    private String nationalityName;

    @Property(name = "gender")
    @Indexed
    private String gender;
    @JsonIgnore
    @Relationship(type = "MANAGES_TEAM", direction = Relationship.Direction.OUTGOING)
    private List<manages_team> teamMNodes = new ArrayList<>();
    @JsonIgnore
    @Relationship(type = "LIKES", direction = Relationship.Direction.INCOMING)
    private List<UsersNode> likesNode = new ArrayList<>();
}
