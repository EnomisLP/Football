package com.example.demo.services.MongoDB;

import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.PlayersNode;

import com.example.demo.projections.PlayersNodeDTO;
import com.example.demo.projections.TeamsNodeDTO;
import com.example.demo.projections.UsersNodeDTO;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Players_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.repositories.Neo4j.Users_node_rep;
import com.example.demo.services.Neo4j.Players_node_service;
import com.example.demo.requets.createPlayerRequest;
import com.example.demo.requets.updateFifaPlayer;
import com.example.demo.requets.updatePlayer;
import com.example.demo.requets.updateTeamPlayer;

import jakarta.transaction.Transactional;
@Service
public class Players_service {

    
    private  Players_repository PMr;
    private Players_node_rep Pmr;
    private Players_node_service PMs;
    private static final Integer CURRENT_YEAR = 24;
    private Teams_repository TMr;
    private Users_node_rep Unr;
    private Teams_node_rep TMs;

    public Players_service(Players_repository PMr, Players_node_rep Pmr, Players_node_service PMs,
            Teams_repository TMr, Users_node_rep Unr, Teams_node_rep TMs) {
        this.PMr = PMr;
        this.Pmr = Pmr;
        this.PMs = PMs;
        this.TMr = TMr;
        this.Unr = Unr;
        this.TMs = TMs;
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
    public Players createPlayer(createPlayerRequest request){
        Players playerM = new Players();
        playerM.setAge(request.getAge());
        playerM.setDob(request.getDob());
        playerM.setGender(request.getGender());
        playerM.setHeight_cm(request.getHeight_cm());
        playerM.setWeight_kg(request.getWeight_kg());
        playerM.setLong_name(request.getLong_name());
        playerM.setNationality_id(request.getNationality_id());
        playerM.setNationality_name(request.getNationality_name());
        playerM.setShort_name(request.getShort_name());
        PMr.save(playerM);
        PlayersNode playerNode = new PlayersNode();
        playerNode.setMongoId(playerM.get_id());
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
            Optional<PlayersNodeDTO> optionalPlayerNode = Pmr.findByMongoIdLight(existingPlayer.get_id());
            if(optionalPlayerNode.isPresent()){
                
                existingPlayer.setAge(playerDetails.getAge());
                existingPlayer.setDob(playerDetails.getDob());
                existingPlayer.setLong_name(playerDetails.getLong_name());
                existingPlayer.setShort_name(playerDetails.getShort_name());
                existingPlayer.setHeight_cm(playerDetails.getHeight_cm());
                existingPlayer.setWeight_kg(playerDetails.getWeight_kg());
                existingPlayer.setGender(playerDetails.getGender());
            
               Pmr.updatePlayerAttributes(
                    existingPlayer.get_id(),
                    playerDetails.getLong_name(),
                    playerDetails.getGender(),
                    playerDetails.getAge()
                );
                
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
    
    @Transactional
    public Players updateFifaPlayer(String id, Integer fifaV, updateFifaPlayer request){
        Optional<Players> optionalPlayer = PMr.findById(id);
        if (optionalPlayer.isPresent()) {
            Players existingPlayer = optionalPlayer.get();
            Optional<PlayersNodeDTO> optionalPlayerNode = Pmr.findByMongoIdLight(existingPlayer.get_id());
            if(optionalPlayerNode.isPresent()){
                List<FifaStatsPlayer> stats = existingPlayer.getFifaStats();
                for(FifaStatsPlayer stat: stats){
                    if(stat.getFifa_version().equals(fifaV)){
                        if(!fifaV.equals(request.getFifa_version())){
                            stat.setFifa_version(request.getFifa_version());
                            Pmr.createPlaysInTeamRelationToTeam(id, stat.getTeam().getTeam_mongo_id(), request.getFifa_version());
                            Pmr.deletePlaysInTeamRelationToTeam(id, stat.getTeam().getTeam_mongo_id(), fifaV);

                            List<UsersNodeDTO> users = Unr.findUsersByMongoIdAndFifaVersion(id, fifaV);
                            if(users != null){
                                for(UsersNodeDTO user : users){
                                    List<PlayersNodeDTO> players = Unr.findHasInMTeamRelationshipsByUsername(user.getUserName());
                                    List<PlayersNodeDTO> fPlayers = Unr.findHasInFTeamRelationshipsByUsername(user.getUserName());
                                    if(existingPlayer.getGender().equals("male")){
                                        if(players != null){
                                            for(PlayersNodeDTO player : players){
                                                Unr.deleteHasInMTeamRelation(user.getUserName(), player.getMongoId());
                                                Unr.createHasInMTeamRelation(user.getUserName(), player.getMongoId(), request.getFifa_version());
                                             
                                            }
                                        }
                                    }
                                    if(existingPlayer.getGender().equals("female")){
                                        if(fPlayers != null){
                                        for(PlayersNodeDTO fPlayer : fPlayers){
                                            Unr.deleteHasInFTeamRelation(user.getUserName(), fPlayer.getMongoId());
                                            Unr.createHasInFTeamRelation(user.getUserName(), fPlayer.getMongoId(), request.getFifa_version());
                                        }
                                    }
                                    }
                                    
                                   
                                }
                            }
                        }
                        stat.setOverall(request.getOverall());
                        stat.setPotential(request.getPotential());
                        stat.setValue_eur(request.getValue_eur());
                        stat.setWage_eur(request.getWage_eur());
                        stat.setClub_position(request.getClub_position());
                        stat.setClub_jersey_number(request.getClub_jersey_number());
                        stat.setClub_contract_valid_until_year(request.getClub_contract_valid_until_year());
                        stat.setLeague_name(request.getLeague_name());
                        stat.setLeague_level(request.getLeague_level());
                        stat.setPace(request.getPace());
                        stat.setShooting(request.getShooting());
                        stat.setDribbling(request.getDribbling());
                        stat.setPassing(request.getPassing());
                        stat.setDefending(request.getDefending());
                        stat.setPhysic(request.getPhysic());
                        stat.setAttacking_crossing(request.getAttacking_crossing());
                        stat.setAttacking_finishing(request.getAttacking_finishing());
                        stat.setAttacking_heading_accuracy(request.getAttacking_heading_accuracy());
                        stat.setAttacking_short_passing(request.getAttacking_short_passing());
                        stat.setAttacking_volleys(request.getAttacking_volleys());
                        stat.setSkill_dribbling(request.getSkill_dribbling());
                        stat.setSkill_curve(request.getSkill_curve());
                        stat.setSkill_fk_accuracy(request.getSkill_fk_accuracy());
                        stat.setSkill_long_passing(request.getSkill_long_passing());
                        stat.setSkill_ball_control(request.getSkill_ball_control());
                        stat.setMovement_acceleration(request.getMovement_acceleration());
                        stat.setMovement_agility(request.getMovement_agility());
                        stat.setMovement_reactions(request.getMovement_reactions());
                        stat.setMovement_balance(request.getMovement_balance());
                        stat.setMovement_sprint_speed(request.getMovement_sprint_speed());
                        stat.setPower_shot_power(request.getPower_shot_power());
                        stat.setPower_jumping(request.getPower_jumping());
                        stat.setPower_stamina(request.getPower_stamina());
                        stat.setPower_strength(request.getPower_strength());
                        stat.setPower_long_shots(request.getPower_long_shots());
                        stat.setMentality_aggression(request.getMentality_aggression());
                        stat.setMentality_interceptions(request.getMentality_interceptions());
                        stat.setMentality_positioning(request.getMentality_positioning());
                        stat.setMentality_vision(request.getMentality_vision());
                        stat.setMentality_penalties(request.getMentality_penalties());
                        stat.setDefending_marking_awareness(request.getDefending_marking_awareness());
                        stat.setDefending_standing_tackle(request.getDefending_standing_tackle());
                        stat.setDefending_sliding_tackle(request.getDefending_sliding_tackle());
                        stat.setGoalkeeping_diving(request.getGoalkeeping_diving());
                        stat.setGoalkeeping_handling(request.getGoalkeeping_handling());
                        stat.setGoalkeeping_kicking(request.getGoalkeeping_kicking());
                        stat.setGoalkeeping_positioning(request.getGoalkeeping_positioning());
                        stat.setGoalkeeping_reflexes(request.getGoalkeeping_reflexes());
                        break;
                    }
                }
                return PMr.save(existingPlayer);
            }
            else{
                throw new RuntimeException("Player with id: " + id+ "not correctly mapped in Neo4j");
            }
        }
        else {
            throw new RuntimeException("Player not found with id: " + id);
        }

    }
    
    @Transactional
    public Players updateTeamPlayer(String id, Integer fifaV, updateTeamPlayer request) {
        Players existingPlayer = PMr.findById(id)
            .orElseThrow(() -> new RuntimeException("Player not found in MongoDB"));

        PlayersNodeDTO existingPlayerNode = Pmr.findByMongoIdLight(existingPlayer.get_id())
            .orElseThrow(() -> new RuntimeException("Player not found in Neo4j"));

        Teams existingTeam = TMr.findById(request.getTeam_mongo_id())
            .orElseThrow(() -> new RuntimeException("Team not found in MongoDB"));

        TeamsNodeDTO existingTeamNode = TMs.findByMongoIdLight(request.getTeam_mongo_id())
            .orElseThrow(() -> new RuntimeException("Team not found in Neo4j"));

        List<FifaStatsPlayer> existingStats = existingPlayer.getFifaStats();
        FifaStatsPlayer targetStat = existingStats.stream()
            .filter(stat -> stat.getFifa_version().equals(fifaV))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Fifa version not found for this player"));

        // Only proceed if team name actually changed
        if (!existingTeam.get_id().equals(targetStat.getTeam().getTeam_mongo_id())) {
        
            Pmr.deletePlaysInTeamRelationToTeam(id, targetStat.getTeam().getTeam_mongo_id(), fifaV);
            Pmr.createPlaysInTeamRelationToTeam(id, request.getTeam_mongo_id(), fifaV);
            targetStat.getTeam().setTeam_name(existingTeam.getTeam_name());
            targetStat.getTeam().setTeam_mongo_id(request.getTeam_mongo_id());
            targetStat.getTeam().setFifa_version(fifaV);
            return PMr.save(existingPlayer);
        }
        return existingPlayer; // No change needed
    }

    //DELETE
    @Transactional
    public void deletePlayer(String id){
        Optional<Players> player = PMr.findById(id);
        Optional<PlayersNodeDTO> playerNode = Pmr.findByMongoIdLight(id);
        if(player.isPresent()){
            PMr.deleteById(id);
        }
        else{
            throw new RuntimeErrorException(null, "Player not found with id: " + id);
        }
        if(playerNode.isPresent()){
            //removing relationship in Neo4j
            PMs.deletePlayer(id);
        }
        else{
            throw new RuntimeErrorException(null, "Player not found with id: " + id);
        }
       
    }

    @Transactional
    public void deleteFifaPlayer(String id, Integer fifaV){
        Optional<Players> player = PMr.findById(id);
        Optional<PlayersNodeDTO> playerNode = Pmr.findByMongoIdLight(id);

        if(player.isPresent()){
            Players existingPlayer = player.get();
            if(playerNode.isPresent()){
                
                List<FifaStatsPlayer> stats = existingPlayer.getFifaStats();
                for(FifaStatsPlayer stat: stats){
                    if(stat.getFifa_version().equals(fifaV)){
                       Pmr.deletePlaysInTeamRelationToTeam(id, stat.getTeam().getTeam_mongo_id(), fifaV);
                        //deleting fifaStats in MongoDB
                        stat.setFifa_version(99);
                        stat.setPlayer_positions("NA");
                        stat.setOverall(-1);
                        stat.setPotential(-1);
                        stat.setValue_eur((long) -1);
                        stat.setClub_position("NA");
                        stat.setClub_jersey_number(-1);
                        stat.setClub_contract_valid_until_year(2999);
                        stat.setLeague_name("DefaultLeague");
                        stat.setLeague_level(-1);

                        stat.getTeam().setTeam_mongo_id("XXXXXXXXXXXX");
                        stat.getTeam().setTeam_name("DefaultTeam");

                        stat.setPace(-1);
                        stat.setShooting(-1);
                        stat.setPassing(-1);
                        stat.setDribbling(-1);
                        stat.setDefending(-1);
                        stat.setPhysic(-1);

                        stat.setAttacking_crossing(-1);
                        stat.setAttacking_finishing(-1);
                        stat.setAttacking_heading_accuracy(-1);
                        stat.setAttacking_short_passing(-1);
                        stat.setAttacking_volleys(-1);

                        stat.setSkill_dribbling(-1);
                        stat.setSkill_curve(-1);
                        stat.setSkill_fk_accuracy(-1);
                        stat.setSkill_long_passing(-1);
                        stat.setSkill_ball_control(-1);

                        stat.setMovement_acceleration(-1);
                        stat.setMovement_sprint_speed(-1);
                        stat.setMovement_agility(-1);
                        stat.setMovement_reactions(-1);
                        stat.setMovement_balance(-1);

                        stat.setPower_shot_power(-1);
                        stat.setPower_jumping(-1);
                        stat.setPower_stamina(-1);
                        stat.setPower_strength(-1);
                        stat.setPower_long_shots(-1);

                        stat.setMentality_aggression(-1);
                        stat.setMentality_interceptions(-1);
                        stat.setMentality_positioning(-1);
                        stat.setMentality_vision(-1);
                        stat.setMentality_penalties(-1);

                        stat.setDefending_marking_awareness(-1);
                        stat.setDefending_standing_tackle(-1);
                        stat.setDefending_sliding_tackle(-1);

                        stat.setGoalkeeping_diving(-1);
                        stat.setGoalkeeping_handling(-1);
                        stat.setGoalkeeping_kicking(-1);
                        stat.setGoalkeeping_positioning(-1);
                        stat.setGoalkeeping_reflexes(-1);
                        break;
                    }
                }
               
                
                PMr.save(existingPlayer);
            }
            else{
                throw new RuntimeErrorException(null, "Player not found with id: " + id);
            }
        }
        else{
            throw new RuntimeErrorException(null, "Player not found with id: " + id);
        }
        

    }

    //OPERATIONS TO SHOW PLAYER STATS
    public FifaStatsPlayer showCurrentYear(String id){
        Optional<Players> optionalPlayer = PMr.findById(id);
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
            throw new RuntimeErrorException(null, "Player with id:"+id+"not found");
        }
    }

    public FifaStatsPlayer showSpecificStats(String id, Integer fifaV){
        Optional<Players> optionalPlayer = PMr.findById(id);
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
            throw new RuntimeErrorException(null, "Player with id:"+id+"not found");
        }
    }
}
