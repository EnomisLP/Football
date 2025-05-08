package com.example.demo.services.MongoDB;

import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.FifaStatsTeam;
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
                existingPlayer.setHeight_cm(playerDetails.getHeight_cm());
                existingPlayer.setWeight_kg(playerDetails.getWeight_kg());
                existingPlayer.setGender(playerDetails.getGender());
            
                existingPlayerNode.setAge(playerDetails.getAge());
                existingPlayerNode.setLongName(playerDetails.getLong_name());
                existingPlayerNode.setGender(playerDetails.getGender());
                if(!playerDetails.getPlayer_id().equals(existingPlayer.getPlayer_id())){
                    List<Teams> teams = TMr.findTeamsByPlayerIdInFifaStats(existingPlayer.getPlayer_id());
                    //Updating MongoDB
                    if(!teams.isEmpty()){
                        for(Teams team : teams){
                            List<FifaStatsTeam> statsTeam = team.getFifaStats();
                            for(FifaStatsTeam stat: statsTeam){
                                if(stat.getLeft_short_free_kick().equals(existingPlayer.getPlayer_id())){
                                    stat.setLeft_short_free_kick(playerDetails.getPlayer_id());
                                }
                                if(stat.getRight_short_free_kick().equals(existingPlayer.getPlayer_id())){
                                    stat.setRight_short_free_kick(playerDetails.getPlayer_id());
                                }
                                if(stat.getShort_free_kick().equals(existingPlayer.getPlayer_id())){
                                    stat.setShort_free_kick(playerDetails.getPlayer_id());
                                }
                                if(stat.getLong_free_kick().equals(existingPlayer.getPlayer_id())){
                                    stat.setLong_free_kick(playerDetails.getPlayer_id());
                                }
                                if(stat.getPenalties().equals(existingPlayer.getPlayer_id())){
                                    stat.setPenalties(playerDetails.getPlayer_id());
                                }
                                if(stat.getLeft_corner().equals(existingPlayer.getPlayer_id())){
                                    stat.setLeft_corner(playerDetails.getPlayer_id());
                                }
                                if(stat.getRight_corner().equals(existingPlayer.getPlayer_id())){
                                    stat.setRight_corner(playerDetails.getPlayer_id());
                                }
                                if(stat.getCaptain().equals(existingPlayer.getPlayer_id())){
                                    stat.setCaptain(playerDetails.getPlayer_id());
                                }
                            }
                            existingPlayerNode.setPlayerId(playerDetails.getPlayer_id());
                            existingPlayer.setPlayer_id(playerDetails.getPlayer_id());
                            TMr.save(team);
                        }
                    }

                }
               
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
                            List<UsersNode> users = Unr.findUsersByPlayerIdAndFifaVersion(existingPlayer.getPlayer_id(),fifaV);
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
    public Players updateTeamPlayer(String id, Integer fifaV, updateTeamPlayer request){
        Optional<Players> optionalPlayer = PMr.findById(id);
        Optional<Teams> optionalTeam1 = TMr.findByTeamId(request.getTeam_id());
        Optional<Teams> optionalTeam2 = TMr.findByTeamName(request.getTeam_name());
        Optional<PlayersNode> optionalPlayerNode = Pmr.findByMongoId(optionalPlayer.get().get_id());
        if(optionalPlayer.isPresent() && optionalPlayerNode.isPresent()){
            PlayersNode existingPlayerNode = optionalPlayerNode.get();
            Players existingPlayer = optionalPlayer.get();
            if(optionalTeam1.isPresent() && optionalTeam2.isPresent()){
                Teams existingTeam1 = optionalTeam1.get();
                Teams existingTeam2 = optionalTeam2.get();
                if(existingTeam1.getTeam_id().equals(existingTeam2.getTeam_id()) ||
                existingTeam1.getTeam_name().equals(existingTeam2.getTeam_name())){
                    List<FifaStatsPlayer> ExistingPlayerstats = existingPlayer.getFifaStats();
                    //Updating MongoDB
                    for(FifaStatsPlayer stat : ExistingPlayerstats){
                        if(stat.getFifa_version().equals(fifaV)){
                            if(stat.getTeamObj().getTeam_id().equals(request.getTeam_id()) ||
                            stat.getTeamObj().getTeam_name().equals(request.getTeam_name())){
                                return existingPlayer;
                            }
                        }
                    }
                    List<Teams> Mongoteams = TMr.findTeamsByPlayerIdInFifaStats(existingPlayer.getPlayer_id());
                    if(!Mongoteams.isEmpty()){
                        //Deleting player_id from teams formation
                        for(Teams MongoTeam : Mongoteams){
                            List<FifaStatsTeam> statsTeam = MongoTeam.getFifaStats();
                            for(FifaStatsTeam stat: statsTeam){
                                if(stat.getLeft_short_free_kick().equals(existingPlayer.getPlayer_id())){
                                    stat.setLeft_short_free_kick(null);
                                }
                                if(stat.getRight_short_free_kick().equals(existingPlayer.getPlayer_id())){
                                    stat.setRight_short_free_kick(null);
                                }
                                if(stat.getShort_free_kick().equals(existingPlayer.getPlayer_id())){
                                    stat.setShort_free_kick(null);
                                }
                                if(stat.getLong_free_kick().equals(existingPlayer.getPlayer_id())){
                                    stat.setLong_free_kick(null);
                                }
                                if(stat.getPenalties().equals(existingPlayer.getPlayer_id())){
                                    stat.setPenalties(null);
                                }
                                if(stat.getLeft_corner().equals(existingPlayer.getPlayer_id())){
                                    stat.setLeft_corner(null);
                                }
                                if(stat.getRight_corner().equals(existingPlayer.getPlayer_id())){
                                    stat.setRight_corner(null);
                                }
                                if(stat.getCaptain().equals(existingPlayer.getPlayer_id())){
                                    stat.setCaptain(null);
                                }
                            }
                            TMr.save(MongoTeam);
                        }
                    }
                    //Updating player_id in teams formation by comparing the positions
                    for(FifaStatsPlayer stat : ExistingPlayerstats){
                        if(stat.getFifa_version().equals(fifaV)){
                            stat.getTeamObj().setTeam_id(request.getTeam_id());
                            stat.getTeamObj().setTeam_name(request.getTeam_name());
                            stat.getTeamObj().setTeam_mongo_id(existingTeam1.get_id());
                            PMr.save(existingPlayer);
                            List<FifaStatsTeam> statsTeam = existingTeam1.getFifaStats();
                            for(FifaStatsTeam Teamstat : statsTeam){
                                if(stat.getFifa_version().equals(fifaV)){
                                    Optional<Players> Capt = PMr.findByPlayerId(Teamstat.getCaptain());
                                    if(Capt.isPresent()){
                                        Players existingCaptain = Capt.get();
                                        List<FifaStatsPlayer> statsCaptain = existingCaptain.getFifaStats();
                                            for(FifaStatsPlayer statCap : statsCaptain){
                                                if(statCap.getFifa_version().equals(fifaV)){
                                                    if(statCap.getClub_position().equals(stat.getClub_position())){
                                                        Teamstat.setCaptain(existingPlayer.getPlayer_id());
                                                        TMr.save(existingTeam1);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Optional<Players> left = PMr.findByPlayerId(Teamstat.getLeft_short_free_kick());
                                    if(left.isPresent()){
                                        Players existingLeft = left.get();
                                        List<FifaStatsPlayer> statsLeft = existingLeft.getFifaStats();
                                            for(FifaStatsPlayer statLeft : statsLeft){
                                                if(statLeft.getFifa_version().equals(fifaV)){
                                                    if(statLeft.getClub_position().equals(stat.getClub_position())){
                                                        Teamstat.setLeft_short_free_kick(existingPlayer.getPlayer_id());
                                                        TMr.save(existingTeam1);
                                                    }
                                                }
                                            }
                                    }
                                    Optional<Players> right = PMr.findByPlayerId(Teamstat.getRight_short_free_kick());
                                    if(right.isPresent()){
                                        Players existingRight = right.get();
                                        List<FifaStatsPlayer> statsRight = existingRight.getFifaStats();
                                            for(FifaStatsPlayer statRight : statsRight){
                                                if(statRight.getFifa_version().equals(fifaV)){
                                                    if(statRight.getClub_position().equals(stat.getClub_position())){
                                                        Teamstat.setRight_short_free_kick(existingPlayer.getPlayer_id());
                                                        TMr.save(existingTeam1);
                                                    }
                                                }
                                            }
                                    }
                                    Optional<Players> shor = PMr.findByPlayerId(Teamstat.getShort_free_kick());
                                    if(shor.isPresent()){
                                        Players existingShort = shor.get();
                                        List<FifaStatsPlayer> statsShort = existingShort.getFifaStats();
                                            for(FifaStatsPlayer statShort : statsShort){
                                                if(statShort.getFifa_version().equals(fifaV)){
                                                    if(statShort.getClub_position().equals(stat.getClub_position())){
                                                        Teamstat.setShort_free_kick(existingPlayer.getPlayer_id());
                                                        TMr.save(existingTeam1);
                                                    }
                                                }
                                            }
                                    }
                                    Optional<Players> longF = PMr.findByPlayerId(Teamstat.getLong_free_kick());
                                    if(longF.isPresent()){
                                        Players existingLong = longF.get();
                                        List<FifaStatsPlayer> statsLong = existingLong.getFifaStats();
                                            for(FifaStatsPlayer statLong : statsLong){
                                                if(statLong.getFifa_version().equals(fifaV)){
                                                    if(statLong.getClub_position().equals(stat.getClub_position())){
                                                        Teamstat.setLong_free_kick(existingPlayer.getPlayer_id());
                                                        TMr.save(existingTeam1);
                                                    }
                                                }
                                            }
                                    }
                                    Optional<Players> pen = PMr.findByPlayerId(Teamstat.getPenalties());
                                    if(pen.isPresent()){
                                        Players existingPen = pen.get();
                                        List<FifaStatsPlayer> statsPen = existingPen.getFifaStats();
                                            for(FifaStatsPlayer statPen : statsPen){
                                                if(statPen.getFifa_version().equals(fifaV)){
                                                    if(statPen.getClub_position().equals(stat.getClub_position())){
                                                        Teamstat.setPenalties(existingPlayer.getPlayer_id());
                                                        TMr.save(existingTeam1);
                                                    }
                                                }
                                            }
                                    }
                                    Optional<Players> leftC = PMr.findByPlayerId(Teamstat.getLeft_corner());
                                    if(leftC.isPresent()){
                                        Players existingLeftC = leftC.get();
                                        List<FifaStatsPlayer> statsLeftC = existingLeftC.getFifaStats();
                                            for(FifaStatsPlayer statLeftC : statsLeftC){
                                                if(statLeftC.getFifa_version().equals(fifaV)){
                                                    if(statLeftC.getClub_position().equals(stat.getClub_position())){
                                                        Teamstat.setLeft_corner(existingPlayer.getPlayer_id());
                                                        TMr.save(existingTeam1);
                                                    }
                                                }
                                            }
                                    }
                                    Optional<Players> rightC = PMr.findByPlayerId(Teamstat.getRight_corner());
                                    if(rightC.isPresent()){
                                        Players existingRightC = rightC.get();
                                        List<FifaStatsPlayer> statsRightC = existingRightC.getFifaStats();
                                            for(FifaStatsPlayer statRightC : statsRightC){
                                                if(statRightC.getFifa_version().equals(fifaV)){
                                                    if(statRightC.getClub_position().equals(stat.getClub_position())){
                                                        Teamstat.setRight_corner(existingPlayer.getPlayer_id());
                                                        TMr.save(existingTeam1);
                                                    }
                                                }
                                            }
                                    }

                                }
                            }
                        }
                    
                    //Updating Neo4j
                    Optional<TeamsNode> optionalTeamNode = TMs.findByTeamId(request.getTeam_id());
                    if(optionalTeamNode.isPresent()){
                        TeamsNode existingTeamNode = optionalTeamNode.get();
                        List<plays_in_team> teams = existingPlayerNode.getTeamMNodes();
                        if(teams != null){
                            for(plays_in_team team : teams){
                                if(team.getFifaV().equals(fifaV)){
                                    existingPlayerNode.getTeamMNodes().remove(team);
                                    plays_in_team newTeam = new plays_in_team(existingTeamNode,fifaV);
                                    existingPlayerNode.getTeamMNodes().add(newTeam);
                                    Pmr.save(existingPlayerNode);
                                }
                            }
                        }
                    }
                    return existingPlayer;
                }
                else{
                    throw new RuntimeErrorException(null, "Teams name and id do not match");
                } 
            }
            else{
                throw new RuntimeErrorException(null, "Team not found with id: " + request.getTeam_id() + " or name: " + request.getTeam_name());
            }
        }
        else{
            throw new RuntimeErrorException(null, "Player not found with id: " + id);
        }

    }
    //DELETE
    @Transactional
    public void deletePlayer(String id){
        Optional<Players> player = PMr.findById(id);
        Optional<PlayersNode> playerNode = Pmr.findByMongoId(id);
        if(player.isPresent()){
            List<Teams> teams = TMr.findTeamsByPlayerIdInFifaStats(player.get().getPlayer_id());
            if(!teams.isEmpty()){
                for(Teams team : teams){
                    List<FifaStatsTeam> statsTeam = team.getFifaStats();
                    for(FifaStatsTeam stat: statsTeam){
                        if(stat.getLeft_short_free_kick().equals(player.get().getPlayer_id())){
                            stat.setLeft_short_free_kick(null);
                        }
                        if(stat.getRight_short_free_kick().equals(player.get().getPlayer_id())){
                            stat.setRight_short_free_kick(null);
                        }
                        if(stat.getShort_free_kick().equals(player.get().getPlayer_id())){
                            stat.setShort_free_kick(null);
                        }
                        if(stat.getLong_free_kick().equals(player.get().getPlayer_id())){
                            stat.setLong_free_kick(null);
                        }
                        if(stat.getPenalties().equals(player.get().getPlayer_id())){
                            stat.setPenalties(null);
                        }
                        if(stat.getLeft_corner().equals(player.get().getPlayer_id())){
                            stat.setLeft_corner(null);
                        }
                        if(stat.getRight_corner().equals(player.get().getPlayer_id())){
                            stat.setRight_corner(null);
                        }
                        if(stat.getCaptain().equals(player.get().getPlayer_id())){
                            stat.setCaptain(null);
                        }
                        TMr.save(team);
                    }
                }
            }
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
                // Delete attribute istances in teams
                List<Teams> teams = TMr.findTeamsByPlayerIdInFifaStats(existingPlayer.getPlayer_id());
                for(Teams team : teams){
                    List<FifaStatsTeam> statsTeam = team.getFifaStats();
                    for(FifaStatsTeam stat: statsTeam){
                        if(stat.getFifa_version().equals(fifaV)){
                            if(stat.getLeft_short_free_kick().equals(existingPlayer.getPlayer_id())){
                                stat.setLeft_short_free_kick(null);
                            }
                            if(stat.getRight_short_free_kick().equals(existingPlayer.getPlayer_id())){
                                stat.setRight_short_free_kick(null);
                            }
                            if(stat.getShort_free_kick().equals(existingPlayer.getPlayer_id())){
                                stat.setShort_free_kick(null);
                            }
                            if(stat.getLong_free_kick().equals(existingPlayer.getPlayer_id())){
                                stat.setLong_free_kick(null);
                            }
                            if(stat.getPenalties().equals(existingPlayer.getPlayer_id())){
                                stat.setPenalties(null);
                            }
                            if(stat.getLeft_corner().equals(existingPlayer.getPlayer_id())){
                                stat.setLeft_corner(null);
                            }
                            if(stat.getRight_corner().equals(existingPlayer.getPlayer_id())){
                                stat.setRight_corner(null);
                            }
                            if(stat.getCaptain().equals(existingPlayer.getPlayer_id())){
                                stat.setCaptain(null);
                            }
                            TMr.save(team);
                            break;
                        }
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
    public FifaStatsPlayer showCurrentYear(Integer playerId){
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

    public FifaStatsPlayer showSpecificStats(Integer playerId, Integer fifaV){
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
