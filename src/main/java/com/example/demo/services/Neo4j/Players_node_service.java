package com.example.demo.services.Neo4j;
import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.PlayersNodeDTO;
import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.PlayersNode;
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
    public PlayersNodeDTO getPlayers(String mongoId) {
        return PMn.findByMongoIdLight(mongoId).orElseThrow(() -> new RuntimeErrorException(null, "Player not found with id: " + mongoId));
    }

     public Page<PlayersNodeDTO> getAllPlayers( String gender, Pageable page){
        return PMn.findAllByGenderWithPagination(gender, page);
    }

   
    private void ensurePlayerNodeIndexes() {
        neo4jClient.query("""
            CREATE INDEX mongoId IF NOT EXISTS FOR (p:PlayersNode) ON (p.mongoId)
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
        Integer nodeToInsert = 0;
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
            playerNode.setNationalityName(player.getNationality_name());
            playerNode.setGender(player.getGender());
            nodeToInsert++;
            PMn.save(playerNode);
        }
        
        return "The amount of PlayersNode created are: " + nodeToInsert;
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
                PMn.createPlaysInTeamRelationToTeam(playerNode.getMongoId(), existingTeam.get_id(), fifaStat.getFifa_version());
                
                    counter++;

                }
            }

        return "Number of relationships created: " + counter;
    }

    // DELETE
    public void deletePlayer(String mongoId) {
        Optional<PlayersNodeDTO> optionalPlayer = PMn.findByMongoIdLight(mongoId);
        if (optionalPlayer.isPresent()) {
            PMn.deletePlayerByMongoIdLight(mongoId);
        } else {
            throw new RuntimeErrorException(null, "Player not found with id: " + mongoId);
        }
    }
    
    //OPERATIONS TO MANAGE TEAM
    public TeamsNodeDTO showCurrentTeam(String playerMongoId){
        Optional<PlayersNodeDTO> optionalPlayerNode = PMn.findByMongoIdLight(playerMongoId);
        
        if(optionalPlayerNode.isPresent() ){
           return PMn.findTeam(playerMongoId,CURRENT_YEAR);
           
        }
        else{
            throw new RuntimeErrorException(null, "Player with id:" + playerMongoId +"not found or team not mapped for current year");
        }
    }

    public TeamsNodeDTO showSpecificTeam(String playerMongoId, Integer fifaVersion){
        Optional<PlayersNodeDTO> optionalPlayerNode = PMn.findByMongoIdLight(playerMongoId);
        
        if(optionalPlayerNode.isPresent() ){
            return PMn.findTeam(playerMongoId, fifaVersion);
           
        }
        else{
            throw new RuntimeErrorException(null, "Player with id:" + playerMongoId +"not found or team not mapped for current year");
        }
    }
    
    public boolean checkLike(String articleId,String username){
        return this.PMn.checkLike(articleId,username);
    }

    public Integer countLike(String id){
        return this.PMn.countLike(id);
    }
}
