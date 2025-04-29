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
import com.example.demo.relationships.plays_in_team;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Players_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;

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
    public PlayersNode getPlayers(Long id) {
        Optional<PlayersNode> optionalPlayer = PMn.findById(id);
        if (optionalPlayer.isPresent()) {
            return optionalPlayer.get();
        } else {
            throw new RuntimeErrorException(null, "Player not found with id: " + id);
        }
    }

     public Page<PlayersNode> getAllPlayers( String gender, PageRequest page){
        return PMn.findAllByGenderWithPagination(gender, page);
    }

    // UPDATE
    public PlayersNode updatePlayer(Long id, PlayersNode playerDetails) {
        Optional<PlayersNode> optionalPlayerNode = PMn.findById(id);
        Optional<Players> optionalPlayer = optionalPlayerNode.flatMap(p -> PMr.findById(p.getMongoId()));
        if (optionalPlayerNode.isPresent() && optionalPlayer.isPresent()) {
            PlayersNode existingPlayerNode = optionalPlayerNode.get();
            Players existingPlayer = optionalPlayer.get();

            // Update Neo4j node
            existingPlayerNode.setPlayerId(playerDetails.getPlayerId());
            existingPlayerNode.setLongName(playerDetails.getLongName());
            existingPlayerNode.setAge(playerDetails.getAge());
            existingPlayerNode.setNationalityName(playerDetails.getNationalityName());
            existingPlayerNode.setGender(playerDetails.getGender());

            // Update MongoDB document
            existingPlayer.setPlayer_id(playerDetails.getPlayerId());
            existingPlayer.setLong_name(playerDetails.getLongName());
            existingPlayer.setAge(playerDetails.getAge());
            existingPlayer.setNationality_name(playerDetails.getNationalityName());
            existingPlayer.setGender(playerDetails.getGender());

            PMr.save(existingPlayer);
            return PMn.save(existingPlayerNode);
        } else {
            throw new RuntimeErrorException(null, "Player with id: " + id + " not correctly mapped on MongoDB or Neo4j");
        }
    }
    private void ensurePlayerNodeIndexes() {
        neo4jClient.query("""
            CREATE INDEX mongoId IF NOT EXISTS FOR (p:PlayersNode) ON (p.mongoId)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX playerId IF NOT EXISTS FOR (p:PlayersNode) ON (p.playerId)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX gender IF NOT EXISTS FOR (p:PlayersNode) ON (p.gender)
        """).run();
    }

    public String MapAllTheNodes() {
        // Ensure indexes are created
        ensurePlayerNodeIndexes();
        List<Players> Allplayers = PMr.findAll();
        List<PlayersNode> nodeToInsert = new ArrayList<>();
        System.out.println("Players found :" + Allplayers.size());
        for (Players player : Allplayers) {
            // Check if the player node already exists in Neo4j
            if (PMn.existsByMongoId(String.valueOf(player.get_id()))) {
                continue;
            }

            // Create a new Neo4j node for the player
            PlayersNode playerNode = new PlayersNode();
            playerNode.setMongoId(player.get_id());
            playerNode.setPlayerId(player.getPlayer_id());
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
        List<PlayersNode> list = PMn.findAllByGender(gender);

        for (PlayersNode playerNode : list) {
            Optional<Players> optionalPlayer = PMr.findById(playerNode.getMongoId());
            if (optionalPlayer.isEmpty()) {
                System.err.printf("No Player found in MongoDB", playerNode.getMongoId());
                continue;
            }

            Players existingPlayer = optionalPlayer.get();
            for (FifaStatsPlayer fifaStat : existingPlayer.getFifaStats()) {
                Optional<Teams> optionalTeam = TMr.findByTeamId(fifaStat.getClub_team_id());
                if (optionalTeam.isEmpty()) {
                    System.err.printf("No Team found in MongoDB", fifaStat.getClub_team_id());
                    continue;
                }

                Teams existingTeam = optionalTeam.get();
                Optional<TeamsNode> optionalTeamNode = TMn.findByMongoId(existingTeam.get_id());

                if (optionalTeamNode.isEmpty()) {
                    System.err.printf("Team not mapped in Neo4j", existingTeam.get_id());
                    continue;
                }

                TeamsNode existingTeamNode = optionalTeamNode.get();
                boolean existingRelationship = false;
                for(plays_in_team test : playerNode.getTeamMNodes()){
                    if(test.alreadyExist(existingTeamNode,fifaStat.getFifa_version())){
                        System.err.println("WARNING: Relationship between Player: "
                        + playerNode.get_id() + " and Team" + existingTeamNode.get_id()
                        +" in fifa Version:"+ fifaStat.getFifa_version()+ " already exist");
                        existingRelationship = true;
                        break; // Skip this player
                    }
                }
                // Check if the relationship already exists
                if(!existingRelationship){
                    plays_in_team relationship = new plays_in_team(existingTeamNode, fifaStat.getFifa_version());
                    playerNode.getTeamMNodes().add(relationship);
                    //PMn.addInTeam(playerNode.getMongoId(), existingTeamNode.getMongoId(), fifaStat.getFifa_version());
                    counter++;
                    PMn.save(playerNode);
                }
            }
        }
        
        return "Number of relationships created: " + counter;
    }

    // DELETE
    public void deletePlayer(Long id) {
        Optional<PlayersNode> optionalPlayer = PMn.findById(id);
        if (optionalPlayer.isPresent()) {
            PlayersNode existing = optionalPlayer.get();
            existing.getTeamMNodes().clear();
            PMn.save(existing);
            PMn.deleteById(id);
        } else {
            throw new RuntimeErrorException(null, "Player not found with id: " + id);
        }
    }
    
    //OPERATIONS TO MANAGE TEAM
    public plays_in_team showCurrentTeam(Integer playerId){
        Optional<PlayersNode> optionalPlayerNode = PMn.findByPlayerId(playerId);
        Optional<plays_in_team> optionalTeam = optionalPlayerNode.flatMap(playerNode -> 
        playerNode.getTeamMNodes().stream().filter(teamNode -> teamNode.getFifaV()
        .equals(CURRENT_YEAR)).findFirst());
        if(optionalPlayerNode.isPresent() && optionalTeam.isPresent()){
            plays_in_team existingTeam = optionalTeam.get();
            return existingTeam;
           
        }
        else{
            throw new RuntimeErrorException(null, "Player with id:" + playerId +"not found or team not mapped for current year");
        }
    }

    public plays_in_team showSpecificTeam(Integer playerId, Integer fifaVersion){
        Optional<PlayersNode> optionalPlayerNode = PMn.findByPlayerId(playerId);
        Optional<plays_in_team> optionalTeam = optionalPlayerNode.flatMap(playerNode -> 
        playerNode.getTeamMNodes().stream().filter(teamNode -> teamNode.getFifaV()
        .equals(fifaVersion)).findFirst());
        if(optionalPlayerNode.isPresent() && optionalTeam.isPresent()){
            plays_in_team existingTeam = optionalTeam.get();
            return existingTeam;
           
        }
        else{
            throw new RuntimeErrorException(null, "Player with id:" + playerId +"not found or team not mapped for current year");
        }
    }

    
}
