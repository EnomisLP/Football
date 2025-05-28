package com.example.demo.models.Neo4j;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.example.demo.relationships.plays_in_team;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Node(labels = "PlayersNode")
@Getter
@Setter
@AllArgsConstructor
@Data
public class PlayersNode{

    public PlayersNode() {
       
    }

    @Id
    @GeneratedValue
    private Long _id;

    @Property(name ="mongoId")
    @Indexed(unique = true)
    private String mongoId;

    @Property(name = "longName")
    private String longName;

    @Property(name = "age")
    private Integer age;

    @Property(name = "nationalityName")
    private String nationalityName;

    @Property(name="gender")
    private String gender;

    @JsonIgnore
    @Relationship(type ="PLAYS_IN_TEAM", direction = Relationship.Direction.OUTGOING)
    private List<plays_in_team> teamMNodes = new ArrayList<>();

    @JsonIgnore
    @Relationship(type = "HAS_IN_M_TEAM", direction = Relationship.Direction.INCOMING)
    private List<UsersNode> playersMNode = new ArrayList<>();
    @JsonIgnore
    @Relationship(type = "HAS_IN_F_TEAM", direction = Relationship.Direction.INCOMING)
    private List<UsersNode> playersFNode = new ArrayList<>();
    @JsonIgnore
    @Relationship(type = "LIKES", direction = Relationship.Direction.INCOMING)
    private List<UsersNode> likesNode = new ArrayList<>();

}
