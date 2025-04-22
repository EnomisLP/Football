package com.example.demo.models.Neo4j;

import java.util.ArrayList;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import com.example.demo.relationships.has_in_F_team;
import com.example.demo.relationships.has_in_M_team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Node(labels = "UsersNode")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersNode {

    @Id
    @GeneratedValue
    private Long _id;

    @Property(name = "mongoId")
    private String mongoId;

    @Property(name = "userName")
    private String userName;

    @Relationship(type = "HAS_IN_M_TEAM", direction = Relationship.Direction.OUTGOING)
    private List<has_in_M_team> playersMNodes = new ArrayList<>();

    @Relationship(type = "HAS_IN_F_TEAM", direction = Relationship.Direction.OUTGOING)
    private List<has_in_F_team> playersFNodes = new ArrayList<>();

    @Relationship(type = "FOLLOWING", direction = Relationship.Direction.OUTGOING)
    private List<UsersNode> followings = new ArrayList<>();

    @Relationship(type = "FOLLOWER", direction = Relationship.Direction.OUTGOING)
    private List<UsersNode> followers = new ArrayList<>();

    @Relationship(type = "LIKES_TEAM", direction = Relationship.Direction.OUTGOING)
    private List<TeamsNode> teamsNodes = new ArrayList<>();

    @Relationship(type = "LIKES_COACH", direction = Relationship.Direction.OUTGOING)
    private List<CoachesNode> coachesNodes = new ArrayList<>();

    @Relationship(type = "LIKES_PLAYER", direction = Relationship.Direction.OUTGOING)
    private List<PlayersNode> playerNodes = new ArrayList<>();

    
}
