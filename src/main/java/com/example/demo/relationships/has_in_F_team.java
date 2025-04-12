package com.example.demo.relationships;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import com.example.demo.models.Neo4j.PlayersNode;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@RelationshipProperties
@AllArgsConstructor
@NoArgsConstructor
public class has_in_F_team{
    @RelationshipId
    @GeneratedValue
    private Long id;

    @Property
    private Integer fifaVersion;

    @TargetNode
    private PlayersNode PFn;

    public has_in_F_team(PlayersNode player, Integer fifaVer){
    this.PFn = player;
    this.fifaVersion = fifaVer;
   }

   public PlayersNode getPlayer(){
    return PFn;
   }

   public Integer getFifaV(){
    return fifaVersion;
   }

   public boolean alreadyExist(PlayersNode player, Integer fifaV){
    return this.PFn.getMongoId().equals(player.getMongoId()) && this.fifaVersion.equals(fifaV);
}

    public boolean alreadyExistPlayer(PlayersNode player){
        if(this.PFn.equals(player)){
            return true;
        }
        else return false;
       }
}