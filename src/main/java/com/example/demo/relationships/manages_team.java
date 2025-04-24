package com.example.demo.relationships;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;


import com.example.demo.models.Neo4j.TeamsNode;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@RelationshipProperties
@AllArgsConstructor
@NoArgsConstructor
public class manages_team {
    @RelationshipId
    @GeneratedValue
    private Long id;
    
    @Property
    private Integer fifaVersion;

    @TargetNode
    private TeamsNode TMn;

    public manages_team(TeamsNode team, Integer fifaV){
    this.TMn = team;
    this.fifaVersion = fifaV;
   }
   public TeamsNode getCoach(){
    return TMn;
   }
   public Integer getFifaV(){
    return fifaVersion;
   }
    public boolean alreadyExist(TeamsNode existingTeam, Integer fifa_version) {
        if(this.TMn.equals(existingTeam) && this.fifaVersion == fifa_version){
        return true;
        }
        else return false;
    }
    public void setFifaV(Integer fifaV){
        this.fifaVersion = fifaV;
    }
   
}
