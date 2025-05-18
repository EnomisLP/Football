package com.example.demo.services.Neo4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.PlayersNode;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.projections.PlayersNodeDTO;
import com.example.demo.relationships.plays_in_team;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Players_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;

import jakarta.transaction.Transactional;

@Service
public class Players_node_service {

    private final Players_node_rep PMn;
    private final Players_repository PMr;
    private final Teams_repository TMr;
    private final Teams_node_rep TMn;
    private final static Integer CURRENT_YEAR = 24;
    @Autowired
    private Neo4jClient neo4jClient;

    public Players_node_service(Players_node_rep pmn, Players_repository pmr,
                                  Teams_repository TMR, Teams_node_rep TMN) {
        this.PMn = pmn;
        this.PMr = pmr;
        this.TMr = TMR;
        this.TMn = TMN;
    }

    // READ
    public PlayersNode getPlayers(String mongoId) {
        Optional<PlayersNode> optionalPlayer = PMn.findByMongoId(mongoId);
        if (optionalPlayer.isPresent()) {
            return optionalPlayer.get();
        } else {
            throw new RuntimeErrorException(null, "Player not found with id: " + mongoId);
        }
    }

     public Page<PlayersNode> getAllPlayers( String gender, PageRequest page){
        return PMn.findAllByGenderWithPagination(gender, page);
    }

   
    private void ensurePlayerNodeIndexes() {
        neo4jClient.query("""
            CREATE INDEX mongoId IF NOT EXISTS FOR (p:PlayersNode) ON (p.mongoId)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX longName IF NOT EXISTS FOR (p:PlayersNode) ON (p.longName)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX gender IF NOT EXISTS FOR (p:PlayersNode) ON (p.gender)
        """).run();
    }

    public String MapAllTheNodes() {
        // Ensure indexes are created
        ensurePlayerNodeIndexes();
        return doMapAllTheNodes();
        
    }
    @Transactional
    public String doMapAllTheNodes(){
        List<Players> Allplayers = PMr.findAll();
        List<PlayersNode> nodeToInsert = new ArrayList<>();
        System.out.println("Players found :" + Allplayers.size());
        for (Players player : Allplayers) {
            // Check if the player node already exists in Neo4j
            if (PMn.existsByMongoId(player.get_id())) {
                continue;
            }

            // Create a new Neo4j node for the player
            PlayersNode playerNode = new PlayersNode();
            playerNode.setMongoId(player.get_id());
            playerNode.setLongName(player.getLong_name());
            playerNode.setAge(player.getAge());
            playerNode.setNationalityName(player.getNationality_name());
            playerNode.setGender(player.getGender());
            nodeToInsert.add(playerNode);
        }
        PMn.saveAll(nodeToInsert);
        return "The amount of PlayersNode created are: " + nodeToInsert.size();
    }
    public String MapAllPlaysInTeamRel(String gender) {
        int counter = 0;
        List<PlayersNodeDTO> list = PMn.findAllLightByGender(gender);

        for (PlayersNodeDTO playerNode : list) {
            System.out.println("PlayerNode: " + playerNode.getMongoId());
             if (playerNode.getMongoId() == null) {
                System.err.println("ERROR: playerNode.getMongoId() is null for playerNode: " + playerNode.getMongoId());
                continue;
            }
            Optional<Players> optionalPlayer = PMr.findById(playerNode.getMongoId());
            if (optionalPlayer.isEmpty()) {
                System.err.printf("No Player found in MongoDB", playerNode.getMongoId());
                continue;
            }

            Players existingPlayer = optionalPlayer.get();
                for (FifaStatsPlayer fifaStat : existingPlayer.getFifaStats()) {
                // Check if teamObj is not null
                    
                    String teamMongoId = fifaStat.getTeam().getTeam_mongo_id();
                    System.out.println("Team mongo ID: " + teamMongoId);
                    if(teamMongoId == null) {
                        System.err.println("Team mongo ID is null for team: " + fifaStat.getTeam().getTeam_name());
                        continue;
                    }
                    Optional<Teams> optionalTeam = TMr.findById(teamMongoId);
                    if (optionalTeam.isEmpty()) {
                        System.out.printf("No Team found in MongoDB for team name:", fifaStat.getTeam().getTeam_name());
                        continue;
                    }

                Teams existingTeam = optionalTeam.get();
                Optional<TeamsNode> optionalTeamNode = TMn.findByMongoId(existingTeam.get_id());
                Optional<PlayersNode> optionalPlayerNode = PMn.findByMongoId(playerNode.getMongoId());
                if (optionalTeamNode.isEmpty() || optionalPlayerNode.isEmpty()) {
                    System.err.printf("No Team or Player found in Neo4j for team name:", existingTeam.get_id());
                    continue;
                }
                PMn.createPlaysInTeamRelationToTeam(playerNode.getMongoId(), existingTeam.get_id(), fifaStat.getFifa_version());
                
                    counter++;
                
                    
                }
            }
        
        return "Number of relationships created: " + counter;
    }

    // DELETE
    public void deletePlayer(String mongoId) {
        Optional<PlayersNode> optionalPlayer = PMn.findByMongoId(mongoId);
        if (optionalPlayer.isPresent()) {
            PlayersNode existing = optionalPlayer.get();
            existing.getTeamMNodes().clear();
            PMn.save(existing);
            PMn.delete(existing);
        } else {
            throw new RuntimeErrorException(null, "Player not found with id: " + mongoId);
        }
    }
    
    //OPERATIONS TO MANAGE TEAM
    public plays_in_team showCurrentTeam(String playerMongoId){
        Optional<PlayersNode> optionalPlayerNode = PMn.findByMongoId(playerMongoId);
        Optional<plays_in_team> optionalTeam = optionalPlayerNode.flatMap(playerNode -> 
        playerNode.getTeamMNodes().stream().filter(teamNode -> teamNode.getFifaV()
        .equals(CURRENT_YEAR)).findFirst());
        if(optionalPlayerNode.isPresent() && optionalTeam.isPresent()){
            plays_in_team existingTeam = optionalTeam.get();
            return existingTeam;
           
        }
        else{
            throw new RuntimeErrorException(null, "Player with id:" + playerMongoId +"not found or team not mapped for current year");
        }
    }

    public plays_in_team showSpecificTeam(String playerMongoId, Integer fifaVersion){
        Optional<PlayersNode> optionalPlayerNode = PMn.findByMongoId(playerMongoId);
        Optional<plays_in_team> optionalTeam = optionalPlayerNode.flatMap(playerNode -> 
        playerNode.getTeamMNodes().stream().filter(teamNode -> teamNode.getFifaV()
        .equals(fifaVersion)).findFirst());
        if(optionalPlayerNode.isPresent() && optionalTeam.isPresent()){
            plays_in_team existingTeam = optionalTeam.get();
            return existingTeam;
           
        }
        else{
            throw new RuntimeErrorException(null, "Player with id:" + playerMongoId +"not found or team not mapped for current year");
        }
    }

    
}
