package com.example.demo.models.Neo4j;

import java.util.ArrayList;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.example.demo.configurations.JSON.UsersNodeJsonComponent;
import com.example.demo.relationships.has_in_F_team;
import com.example.demo.relationships.has_in_M_team;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Node(labels = "UsersNode")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = UsersNodeJsonComponent.UsersNodeSerializer.class)
@JsonDeserialize(using = UsersNodeJsonComponent.UsersNodeDeserializer.class)
public class UsersNode {

    @Id
    @GeneratedValue
    private Long _id;

    @Property(name = "mongoId")
    @Indexed(unique = true)
    private String mongoId;

    @Property(name = "userName")
    @Indexed(unique = true)
    private String userName;
   
    @JsonIgnore
    @Relationship(type = "HAS_IN_M_TEAM", direction = Relationship.Direction.OUTGOING)
    private List<has_in_M_team> playersMNodes = new ArrayList<>();
   
     @JsonIgnore
    @Relationship(type = "HAS_IN_F_TEAM", direction = Relationship.Direction.OUTGOING)
    private List<has_in_F_team> playersFNodes = new ArrayList<>();
    
     @JsonIgnore
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private List<UsersNode> followings = new ArrayList<>();

     @JsonIgnore
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.INCOMING)
    private List<UsersNode> followers = new ArrayList<>();
    
    @JsonIgnore
    @Relationship(type = "LIKES", direction = Relationship.Direction.OUTGOING)
    private List<TeamsNode> teamsNodes = new ArrayList<>();
    
     @JsonIgnore
    @Relationship(type = "LIKES", direction = Relationship.Direction.OUTGOING)
    private List<CoachesNode> coachesNodes = new ArrayList<>();
    
     @JsonIgnore
    @Relationship(type = "LIKES", direction = Relationship.Direction.OUTGOING)
    private List<PlayersNode> playerNodes = new ArrayList<>();
    
     @JsonIgnore
    @Relationship(type = "WROTE", direction = Relationship.Direction.OUTGOING)
    private List<ArticlesNode> articlesNodes = new ArrayList<>();
    
     @JsonIgnore
    @Relationship(type = "LIKES", direction = Relationship.Direction.OUTGOING)
    private List<ArticlesNode> likedArticlesNodes = new ArrayList<>();
}
