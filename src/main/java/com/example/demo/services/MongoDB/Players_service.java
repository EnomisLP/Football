package com.example.demo.services.MongoDB;

import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.Neo4j.PlayersNode;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.Neo4j.Players_node_rep;
import com.example.demo.services.Neo4j.Players_node_service;
import com.example.demo.requets.updatePlayer;

import jakarta.transaction.Transactional;
@Service
public class Players_service {

    
    private  Players_repository PMr;
    private Players_node_rep Pmr;
    private Players_node_service PMs;
    private static final Integer CURRENT_YEAR = 24;

    public Players_service(Players_repository PMr, Players_node_rep Pmr, Players_node_service PMs) {
        this.PMr = PMr;
        this.Pmr = Pmr;
        this.PMs = PMs;
    }
    
    //READ
   public Players getPlayer(String id){
        Optional<Players> player = PMr.findById(id);
        if(player.isPresent()){
            return player.get();
        }
        else{
            throw new RuntimeErrorException(null, "Player not found with id: " + id);
        }
    }
    public List<PlayersNode> getAllPlayers(String gender){
        return Pmr.findAllByGender(gender);
    }

    public Page<Players> getAllPlayers(PageRequest page, String gender){
        return PMr.findAllByGender(gender, page);
    }
    //CREATE
    @Transactional
    public Players createPlayer(Players player){
        Players playerM = PMr.save(player);
        PlayersNode playerNode = new PlayersNode();
        playerNode.setMongoId(playerM.get_id());
        playerNode.setPlayerId(playerM.getPlayer_id());
        playerNode.setLongName(playerM.getLong_name());
        playerNode.setAge(playerM.getAge());
        playerNode.setNationalityName(playerM.getNationality_name());
        playerNode.setGender(playerM.getGender());
        Pmr.save(playerNode);
        return playerM;
    };

    //UPDATE
    @Transactional
    public Players updatePlayer(String id, updatePlayer playerDetails) {
        Optional<Players> optionalPlayer = PMr.findById(id);
        if (optionalPlayer.isPresent()) {
            Players existingPlayer = optionalPlayer.get();
            Optional<PlayersNode> optionalPlayerNode = Pmr.findByMongoId(existingPlayer.get_id());
            if(optionalPlayerNode.isPresent()){
                PlayersNode existingPlayerNode = optionalPlayerNode.get();
                existingPlayer.setAge(playerDetails.getAge());
                existingPlayer.setDob(playerDetails.getDob());
                existingPlayer.setLong_name(playerDetails.getLong_name());
                existingPlayer.setShort_name(playerDetails.getShort_name());
                existingPlayer.setNationality_name(playerDetails.getNationality_name());
                existingPlayer.setHeight_cm(playerDetails.getHeight_cm());
                existingPlayer.setWeight_kg(playerDetails.getWeight_kg());
                existingPlayer.setGender(playerDetails.getGender());
            
                existingPlayerNode.setAge(playerDetails.getAge());
                existingPlayerNode.setLongName(playerDetails.getLong_name());
                existingPlayerNode.setNationalityName(playerDetails.getNationality_name());
                existingPlayerNode.setGender(playerDetails.getGender());
            
                Pmr.save(existingPlayerNode);
                // Save the updated player back to the repository
                return PMr.save(existingPlayer);
            }
            else{
                throw new RuntimeException("Player with id: " + id+ "not correctly mapped in Neo4j");
            }
        
        } else {
            throw new RuntimeException("Player not found with id: " + id);
        }
    }
    //DELETE
    @Transactional
    public void deletePlayer(String id){
        Optional<Players> player = PMr.findById(id);
        Optional<PlayersNode> playerNode = Pmr.findByMongoId(id);
        if(player.isPresent()){
            PMr.deleteById(id);
        }
        if(playerNode.isPresent()){
            PlayersNode existing = playerNode.get();
            PMs.deletePlayer(existing.get_id());
        }
        else{
            throw new RuntimeErrorException(null, "Player not found with id: " + id);
        }
       
    }

    //OPERATIONS TO SHOW PLAYER STATS
    public FifaStatsPlayer showCurrentYear(Long playerId){
        Optional<Players> optionalPlayer = PMr.findByPlayerId(playerId);
        if(optionalPlayer.isPresent()){
            Players existingPlayer = optionalPlayer.get();
            Optional<FifaStatsPlayer> optionalFifaStats = existingPlayer.getFifaStats().
            stream().filter(fifaStat -> fifaStat.getFifa_version().
            equals(CURRENT_YEAR)).findFirst();
            if(optionalFifaStats.isPresent()){
                return optionalFifaStats.get();
            }
            else{
                throw new RuntimeErrorException(null, "Fifa stats not correctly mapped for last year");
            }
        }
        else{
            throw new RuntimeErrorException(null, "Player with id:"+playerId+"not found");
        }
    }

    public FifaStatsPlayer showSpecificStats(Long playerId, Integer fifaV){
        Optional<Players> optionalPlayer = PMr.findByPlayerId(playerId);
        if(optionalPlayer.isPresent()){
            Players existingPlayer = optionalPlayer.get();
            Optional<FifaStatsPlayer> optionalFifaStats = existingPlayer.getFifaStats().
            stream().filter(fifaStat -> fifaStat.getFifa_version().
            equals(fifaV)).findFirst();
            if(optionalFifaStats.isPresent()){
                return optionalFifaStats.get();
            }
            else{
                throw new RuntimeErrorException(null, "Fifa stats not available for that year");
            }
        }
        else{
            throw new RuntimeErrorException(null, "Player with id:"+playerId+"not found");
        }
    }
}
