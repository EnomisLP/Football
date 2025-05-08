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
public class plays_in_team {
    @RelationshipId
    @GeneratedValue
    private Long id;
    
    @Property
    private Integer fifaVersion;

    @TargetNode
    private TeamsNode TMn;

    public plays_in_team(TeamsNode team, Integer fifaV){
    this.TMn = team;
    this.fifaVersion = fifaV;
   }
   public TeamsNode getTeam(){
    return TMn;
   }
   public Integer getFifaV(){
    return fifaVersion;
   }
   public boolean alreadyExist(TeamsNode team, Integer fifaV){
    if(this.TMn.equals(team) && this.fifaVersion == fifaV){
        return true;
    }
    else return false;
   }
   public void setFifaV(Integer fifa_version) {
    this.fifaVersion = fifa_version;
   }
   public void  setTeam_id(Long team_id) {
    this.TMn.setTeamId(team_id);
   }
   public void setTeam_name(String team_name) {
    this.TMn.setTeamName(team_name);
   }
}
