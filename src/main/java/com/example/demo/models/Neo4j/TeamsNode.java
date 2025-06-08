package com.example.demo.models.Neo4j;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @Property(name ="longName")
    private String longName;
    @Property(name="gender")
    private String gender;
    @JsonIgnore
    @Relationship(type = "PLAYS_IN_TEAM", direction = Relationship.Direction.INCOMING)
    private List<PlayersNode> playersNode = new ArrayList<>();
    @JsonIgnore
    @Relationship(type = "MANAGES_TEAM", direction = Relationship.Direction.INCOMING)
    private List<CoachesNode> managersNode = new ArrayList<>();
    @Relationship(type = "LIKES", direction = Relationship.Direction.INCOMING)
    private List<UsersNode> likesNode = new ArrayList<>();

}

