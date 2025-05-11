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
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.relationships.has_in_F_team;
import com.example.demo.relationships.has_in_M_team;
import com.example.demo.relationships.plays_in_team;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Players_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.repositories.Neo4j.Users_node_rep;
import com.example.demo.services.Neo4j.Players_node_service;
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
                existingPlayer.setHeight_cm(playerDetails.getHeight_cm());
                existingPlayer.setWeight_kg(playerDetails.getWeight_kg());
                existingPlayer.setGender(playerDetails.getGender());
            
                existingPlayerNode.setAge(playerDetails.getAge());
                existingPlayerNode.setLongName(playerDetails.getLong_name());
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
    
    @Transactional
    public Players updateFifaPlayer(String id, Integer fifaV, updateFifaPlayer request){
        Optional<Players> optionalPlayer = PMr.findById(id);
        if (optionalPlayer.isPresent()) {
            Players existingPlayer = optionalPlayer.get();
            Optional<PlayersNode> optionalPlayerNode = Pmr.findByMongoId(existingPlayer.get_id());
            if(optionalPlayerNode.isPresent()){
                PlayersNode existingPlayerNode = optionalPlayerNode.get();
                List<FifaStatsPlayer> stats = existingPlayer.getFifaStats();
                for(FifaStatsPlayer stat: stats){
                    if(stat.getFifa_version().equals(fifaV)){
                        if(!fifaV.equals(request.getFifa_version())){
                            stat.setFifa_version(request.getFifa_version());
                            List<plays_in_team> teams = existingPlayerNode.getTeamMNodes();
                            if(teams != null){
                                for(plays_in_team team : teams){
                                    if(team.getFifaV().equals(fifaV)){
                                        team.setFifaV(request.getFifa_version());
                                        Pmr.save(existingPlayerNode);
                                        break;
                                    }
                                }
                            }
                            List<UsersNode> users = Unr.findUsersByLongNameAndFifaVersion(existingPlayer.getLong_name(),fifaV);
                            if(users != null){
                                for(UsersNode user : users){
                                    Optional<UsersNode> hydratatedUser = Unr.findByUserName(user.getUserName());
                                    if(hydratatedUser.isPresent()){
                                        UsersNode existingUser = hydratatedUser.get();
                                        List<has_in_M_team> teamsM = existingUser.getPlayersMNodes();
                                        if(teamsM != null){
                                            for(has_in_M_team team : teamsM){
                                                if(team.getFifaV().equals(fifaV)){
                                                    team.setFifaV(request.getFifa_version());
                                                    Unr.save(existingUser);
                                                    break;
                                                }
                                            }
                                        }
                                        List<has_in_F_team> teamsF = existingUser.getPlayersFNodes();
                                        if(teamsF != null){
                                            for(has_in_F_team team : teamsF){
                                                if(team.getFifaV().equals(fifaV)){
                                                    team.setFifaV(request.getFifa_version());
                                                    Unr.save(existingUser);
                                                    break;
                                                }
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
                        stat.setMovement_sprintSpeed(request.getMovement_sprintSpeed());
                        stat.setMovement_agility(request.getMovement_agility());
                        stat.setMovement_reactions(request.getMovement_reactions());
                        stat.setMovement_balance(request.getMovement_balance());
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

    PlayersNode existingPlayerNode = Pmr.findByMongoId(existingPlayer.get_id())
        .orElseThrow(() -> new RuntimeException("Player not found in Neo4j"));

    Teams existingTeam = TMr.findByTeamName(request.getTeam_name())
        .orElseThrow(() -> new RuntimeException("Team not found in MongoDB"));

    TeamsNode existingTeamNode = TMs.findByTeamName(request.getTeam_name())
        .orElseThrow(() -> new RuntimeException("Team not found in Neo4j"));

    List<FifaStatsPlayer> existingStats = existingPlayer.getFifaStats();
    FifaStatsPlayer targetStat = existingStats.stream()
        .filter(stat -> stat.getFifa_version().equals(fifaV))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Fifa version not found for this player"));

    // Only proceed if team name actually changed
    if (!existingTeam.getTeam_name().equals(targetStat.getTeam().getTeam_name())) {
        List<plays_in_team> teams = existingPlayerNode.getTeamMNodes();
        if (teams == null || teams.isEmpty()) {
            throw new RuntimeException("Player has no teams in Neo4j");
        }

        // Remove the old team mapping for the fifaVersion
        teams.removeIf(team -> team.getFifaV().equals(fifaV));

        // Add the new one
        plays_in_team newTeamRelation = new plays_in_team(existingTeamNode, fifaV);
        teams.add(newTeamRelation);
        existingPlayerNode.setTeamMNodes(teams);  // Ensure list is set after modification
        Pmr.save(existingPlayerNode);

        // Update MongoDB reference
        targetStat.getTeam().setTeam_name(request.getTeam_name());
        return PMr.save(existingPlayer);
    }

    return existingPlayer; // No change needed
}

    //DELETE
    @Transactional
    public void deletePlayer(String id){
        Optional<Players> player = PMr.findById(id);
        Optional<PlayersNode> playerNode = Pmr.findByMongoId(id);
        if(player.isPresent()){
            PMr.deleteById(id);
        }
        else{
            throw new RuntimeErrorException(null, "Player not found with id: " + id);
        }
        if(playerNode.isPresent()){
            PlayersNode existing = playerNode.get();
            PMs.deletePlayer(existing.get_id());
        }
        else{
            throw new RuntimeErrorException(null, "Player not found with id: " + id);
        }
       
    }

    @Transactional
    public void deleteFifaPlayer(String id, Integer fifaV){
        Optional<Players> player = PMr.findById(id);
        Optional<PlayersNode> playerNode = Pmr.findByMongoId(id);

        if(player.isPresent()){
            Players existingPlayer = player.get();
            if(playerNode.isPresent()){
                PlayersNode existing = playerNode.get();
                List<FifaStatsPlayer> stats = existingPlayer.getFifaStats();
                for(FifaStatsPlayer stat: stats){
                    if(stat.getFifa_version().equals(fifaV)){
                        //removing relationship in Neo4j
                        for(plays_in_team team : existing.getTeamMNodes()){
                            if(team.getFifaV().equals(stat.getFifa_version())){
                               existing.getTeamMNodes().remove(team);
                               Pmr.save(existing);
                               break;
                            }
                        }
                        //deleting fifaStats in MongoDB
                        existingPlayer.getFifaStats().remove(stat);
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
