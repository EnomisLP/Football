package com.example.demo.services.MongoDB;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.management.RuntimeErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.CoachesNodeDTO;
import com.example.demo.DTO.PlayersNodeDTO;
import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.FifaStatsTeam;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.TeamObj;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.repositories.MongoDB.Coaches_repository;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.repositories.Neo4j.Players_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.requets.updateTeam;
import com.example.demo.services.Neo4j.Teams_node_service;
import com.example.demo.requets.updateFifaTeam;
import com.example.demo.requets.createTeamRequest;
import com.example.demo.requets.updateCoachTeam;

import jakarta.transaction.Transactional;

@Service
public class Teams_service {

    private Teams_repository TMr;
    private Teams_node_rep Tmr;
    private static final Integer CURRENT_YEAR = 24;
    private Players_repository PMr;
    private Coaches_repository CMr;
    private Coaches_node_rep CMn;
    private Players_node_rep Pmn;
    private Teams_node_service TMs;

    public Teams_service(Teams_repository tmr, Teams_node_rep TMR,
    Players_repository pmr, Coaches_repository cmr, Coaches_node_rep CMn,
    Players_node_rep pmn, Teams_node_service teams_node_service) {
        this.TMr = tmr;
        this.Tmr = TMR;
        this.PMr = pmr;
        this.CMr = cmr;
        this.CMn = CMn;
        this.Pmn = pmn;
        this.TMs = teams_node_service;
    }

    //READ
    public Teams getTeam(String id){
        Optional<Teams> team = TMr.findById(id);
        if(team.isPresent()){
            return team.get();
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
    }

    public Page<Teams> getAllTeams(PageRequest page, String gender){
        return TMr.findAllByGender(gender, page);
    }
    //CREATE
    @Async("customAsyncExecutor")
    @Transactional
    public CompletableFuture<Teams> createTeam(createTeamRequest request){
        Teams teamM = new Teams();
        teamM.setGender(request.getGender());
        teamM.setLeague_level(request.getLeague_level());
        teamM.setLeague_name(request.getLeague_name());
        teamM.setTeam_name(request.getTeam_name());
        teamM.setNationality_name(request.getNationality_name());
        TMr.save(teamM);
        TeamsNode newTeam = new TeamsNode();
        newTeam.setMongoId(teamM.get_id());
        newTeam.setLongName(teamM.getTeam_name());
        newTeam.setGender(teamM.getGender());
        Tmr.save(newTeam);
        return CompletableFuture.completedFuture(teamM);
    }

    //UPDATE
    @Transactional
    @Async("customAsyncExecutor")
    public CompletableFuture<Teams> updateTeam(String id, updateTeam teamsDetails) {
        Optional<Teams> optionalTeam = TMr.findById(id);
        if (optionalTeam.isPresent()) {
            Teams existingTeam = optionalTeam.get();
            Optional<TeamsNodeDTO> optionalTeamNode = Tmr.findByMongoIdLight(existingTeam.get_id());
            if(optionalTeamNode.isPresent()){
                if(!existingTeam.getTeam_name().equals(teamsDetails.getTeam_name())){
                    //Updating attribute istances
                    List<Players> players = PMr.findByClubTeamMongoIdInFifaStats(existingTeam.get_id());
                    if(!players.isEmpty()){
                        for (Players player : players) {
                            List <FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                            
                            for (FifaStatsPlayer playerFifaStat : playerFifaStats) {
                                if(playerFifaStat.getTeam().getTeam_mongo_id() == null){
                                    continue;
                                }
                                System.out.println("found! " + player.getLong_name());
                                if (!playerFifaStat.getTeam().getTeam_mongo_id().equals(id)){
                                    continue;
                                }
                                playerFifaStat.getTeam().setTeam_name(teamsDetails.getTeam_name());
                                existingTeam.setTeam_name(teamsDetails.getTeam_name());
                                PMr.save(player);
                            }
                        }
                       
                    }
                    
                    List<Coaches> coaches = CMr.findByTeamMongoIdInTeams(existingTeam.get_id());
                    if(!coaches.isEmpty()){
                        System.out.println("Coaches found" + coaches.size());
                        for(Coaches coach : coaches){
                            List<TeamObj> teams = coach.getTeam();
                            for(TeamObj team : teams){
                                if (!team.getTeam_mongo_id().equals(id)){
                                    continue;
                                }
                                
                                team.setTeam_name(teamsDetails.getTeam_name());
                                CMr.save(coach);
                            }
                        }
                    }
                }
                existingTeam.setTeam_name(teamsDetails.getTeam_name());
                existingTeam.setGender(teamsDetails.getGender());
                
                existingTeam.setLeague_level(teamsDetails.getLeague_level());
                existingTeam.setLeague_name(teamsDetails.getLeague_name());
                
                existingTeam.setNationality_name(teamsDetails.getNationality_name());
                
                Tmr.updateTeamGender(id, teamsDetails.getGender());
                Tmr.updateTeamName(id, teamsDetails.getTeam_name());

                // Save the updated team back to the repository
                return CompletableFuture.completedFuture(TMr.save(existingTeam));
            }
            else{
                throw new RuntimeException("Team with id: " + id+" not correctly mapped in Neo4j");
            }
        
        } else {
        throw new RuntimeException("Team not found with id: " + id);
        }
    }

    @Async("customAsyncExecutor")
    @Transactional
    public CompletableFuture<Teams> updateFifaTeam(String id, Integer fifaV, updateFifaTeam request) {
        Optional<Teams> optionalTeam = TMr.findById(id);
        if(optionalTeam.isPresent()){
            Teams existingTeam = optionalTeam.get();
            List<FifaStatsTeam> existingFifaStatsTeam = existingTeam.getFifaStats();
            for(FifaStatsTeam stat : existingFifaStatsTeam){
                if(stat.getFifa_version().equals(fifaV)){
                    if(!stat.getFifa_version().equals(request.getFifa_version())){
                        stat.setFifa_version(request.getFifa_version());

                        //Updating relationship in Neo4j
                        Optional<CoachesNodeDTO> Coaches = CMn.findFifaVersionByMongoIdAndFifaV(id, fifaV);
                        Optional<TeamsNodeDTO> Teams = CMn.findTeamsbyFifaVAndMongoId(stat.getCoach().getCoach_mongo_id(), fifaV);
                        if(Coaches.isPresent() && Teams.isPresent() ){
                            CMn.createManagesRelationToTeam(stat.getCoach().getCoach_mongo_id(), id, request.getFifa_version());
                            CMn.deleteManagesRelationToTeam(stat.getCoach().getCoach_mongo_id(), id, fifaV);
                        }
                       

                        //Updating attribute istances
                        List<Players> players = PMr.findByClubTeamMongoIdInFifaStats(existingTeam.get_id());
                        if(players.isEmpty()){
                            throw new RuntimeErrorException(null, "Players not found in team with id: " + id);
                        }
                        for(Players player : players){
                            List<FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                            for(FifaStatsPlayer playerFifaStat : playerFifaStats){
                                if(playerFifaStat.getFifa_version().equals(fifaV)){
                                    Optional<PlayersNodeDTO> optionalPlayersNode = Pmn.findByMongoIdLight(player.get_id());
                                    if(optionalPlayersNode.isPresent()){
                                        Pmn.createPlaysInTeamRelationToTeam(player.get_id(), id, request.getFifa_version());
                                        Pmn.deletePlaysInTeamRelationToTeam(player.get_id(), id, fifaV);
                                    }
                                    playerFifaStat.setFifa_version(request.getFifa_version());
                                    PMr.save(player);
                                    break;
                                }
                            }
                        }
                        List<Coaches> coaches = CMr.findByTeamMongoIdInTeams(existingTeam.get_id());
                        System.out.println("coaches found" + coaches.size());
                        for(Coaches coach : coaches){
                            List<TeamObj> teams = coach.getTeam();
                            for(TeamObj team : teams){
                                if(team.getFifa_version().equals(fifaV)){
                                    team.setFifa_version(request.getFifa_version());
                                    CMr.save(coach);
                                    break;
                                }
                            }
                        }
                    }
                        stat.setHome_stadium(request.getHome_stadium());
                        stat.setOverall(request.getOverall());
                        stat.setClub_worth_eur(request.getClub_worth_eur());
                        stat.setAttack(request.getAttack());
                        stat.setDefence(request.getDefence());
                        stat.setMidfield(request.getMidfield());
                        break;
                }
                else{
                    continue;
                }
            }
            return CompletableFuture.completedFuture(TMr.save(existingTeam));
            
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
    }
    
    @Async("customAsyncExecutor")
    @Transactional
    public CompletableFuture<Teams> updateCoachTeam(String id, Integer fifaV, updateCoachTeam request){
        Optional<Teams> optionalTeam = TMr.findById(id);
        Optional<TeamsNodeDTO> optionalTeamNode = Tmr.findByMongoIdLight(id);
        Optional<Coaches> optionalCoach = CMr.findById(request.getCoach_mongo_id());
        if(optionalTeam.isPresent() && optionalTeamNode.isPresent()){
            Teams existingTeam = optionalTeam.get();
            List<FifaStatsTeam> existingFifaStats = existingTeam.getFifaStats();
            if( optionalCoach.isPresent()){
                
                Coaches existingCoach = optionalCoach.get();
                for(FifaStatsTeam stats : existingFifaStats){
                    if(stats.getFifa_version().equals(fifaV)){
                        if(request.getCoach_mongo_id().equals(stats.getCoach().getCoach_mongo_id())){
                            return CompletableFuture.completedFuture(existingTeam);
                        }
                        List<TeamObj> teams = existingCoach.getTeam();
                        for(TeamObj team : teams){
                            if(team.getFifa_version().equals(fifaV)){
                                team.setTeam_name(existingTeam.getTeam_name());
                                team.setTeam_mongo_id(existingTeam.get_id());
                                team.setFifa_version(fifaV);
                                break;
                            }
                        }
                        CMr.save(existingCoach);
                            
                        //Update Coach Neo4j
                        List<Coaches> optCoach = CMr.findByTeamMongoIdInTeams(existingTeam.get_id());
                        for(Coaches coach : optCoach){
                            for(TeamObj team : teams){
                                if(team.getFifa_version().equals(fifaV)){
                                    Optional<CoachesNodeDTO> optionalCoachNodeOld = CMn.findByMongoIdLight(coach.get_id());
                                    if(optionalCoachNodeOld.isPresent()){
                                        CMn.deleteManagesRelationToTeam(coach.get_id(), team.getTeam_mongo_id(), fifaV);
                                        
                                    }
                                    else{
                                        throw new RuntimeErrorException(null, "Coach not found with name: " + coach.getLong_name());
                                }
                                break;
                            }
                        }
                    }
                           
                            Optional<CoachesNodeDTO> optionalCoachNode = CMn.findByMongoIdLight(existingCoach.get_id());
                            if(optionalCoachNode.isPresent()){
                                CMn.createManagesRelationToTeam(existingCoach.get_id(), existingTeam.get_id(), fifaV);
                            }
                            else{
                                throw new RuntimeErrorException(null, "Coach not found with id: " + request.getCoach_mongo_id());
                            }
                            //Update Team MongoDB
                            
                            stats.getCoach().setCoach_name(existingCoach.getLong_name());
                            stats.getCoach().setCoach_mongo_id(existingCoach.get_id());
                            TMr.save(existingTeam);
                        }

                    
                    }
                    return CompletableFuture.completedFuture(existingTeam);
                
                
            }
            else{
                throw new RuntimeErrorException(null, "Coach not found with id: " + request.getCoach_mongo_id());
            }

        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }

    }  
    //DELETE
    @Async("customAsyncExecutor")
    @Transactional
    public CompletableFuture<String> deleteTeam(String id){
        Optional<Teams> team = TMr.findById(id);
        Optional<TeamsNodeDTO> teamsNode = Tmr.findByMongoIdLight(id);
        if(team.isPresent()){
            Teams existingTeam = team.get();

            //Deleting attribute istances
            List<Players> players = PMr.findByClubTeamMongoIdInFifaStats(existingTeam.get_id());
            if(!players.isEmpty()){
                for (Players player : players) {
                    List <FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                    for (FifaStatsPlayer playerFifaStat : playerFifaStats) {
                        playerFifaStat.getTeam().setTeam_name("DefaultTeam");
                        playerFifaStat.getTeam().setTeam_mongo_id("DefaultTeam");
                        playerFifaStat.setClub_position("NA");
                        playerFifaStat.setLeague_name("DefaultLeague");
                        playerFifaStat.setClub_contract_valid_until_year(2999);
                        playerFifaStat.setClub_jersey_number(-1);
                        playerFifaStat.setLeague_level(-1);
                        playerFifaStat.setFifa_version(-1);
                        PMr.save(player);
                    }
                }
            }

            List<Coaches> coaches = CMr.findByTeamMongoIdInTeams(existingTeam.get_id());
            if(!coaches.isEmpty()){
                for(Coaches coach : coaches){
                    List<TeamObj> teams = coach.getTeam();
                    for(TeamObj Team : teams){
                        Team.setTeam_name("DefaultTeam");
                        Team.setTeam_mongo_id("XXXXXXXXXXXX");
                        Team.setFifa_version(-1);
                        CMr.save(coach);
                    }
                }
            }
            //deleting team in mongoDB
            TMr.deleteById(id);
        }
        //Deleting team in Neo4j
        if(teamsNode.isPresent()){
            Tmr.deleteByMongoIdLight(id);
            return CompletableFuture.completedFuture("Deleted!");
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
        
    }
    
    @Async("customAsyncExecutor")
    @Transactional
    public CompletableFuture<String> deleteFifaTeam(String id, Integer fifaV){
        Optional<Teams> team = TMr.findById(id);
        Optional<TeamsNodeDTO> teamsNode = Tmr.findByMongoIdLight(id);
        if(team.isPresent() && teamsNode.isPresent()){
            Teams existingTeam = team.get();
            List<FifaStatsTeam> existingFifaStats = existingTeam.getFifaStats();
            //removing relationship in Neo4j
            if(!existingFifaStats.isEmpty()) {
                for (FifaStatsTeam fifaStat : existingFifaStats) {
                    if (fifaStat.getFifa_version().equals(fifaV)) {
                        Optional<CoachesNodeDTO> optionalCoach = CMn.findByMongoIdLight(fifaStat.getCoach().getCoach_mongo_id());
                        if(optionalCoach.isPresent()){
                            CMn.deleteManagesRelationToTeam( fifaStat.getCoach().getCoach_mongo_id(),id, fifaV);
                            break;
                        }
                        else{
                            throw new RuntimeErrorException(null, "Coach not found with id: " + fifaStat.getCoach().getCoach_mongo_id());
                        }
                        
                    }
                }
                //Deleting fifa stats from MongoDB
                for (FifaStatsTeam fifaStat : existingFifaStats) {
                    if (fifaStat.getFifa_version().equals(fifaV)) {
                        fifaStat.setFifa_version(-1);
                        fifaStat.setHome_stadium("DefaultStadiumName");
                        fifaStat.setOverall(-1);
                        fifaStat.setAttack(-1);
                        fifaStat.setMidfield(-1);
                        fifaStat.setDefence(-1);
                        fifaStat.setClub_worth_eur((long) -1);
                        fifaStat.getCoach().setCoach_mongo_id("XXXXXXXXXXXX");
                        fifaStat.getCoach().setCoach_name("DefaultCoachName");

                        break;
                    }
                }
                TMr.save(existingTeam);

                //Deleting attribute istances
                
                List<Players> players = PMr.findByClubTeamMongoIdInFifaStats(existingTeam.get_id());
                if(!players.isEmpty()){
                    for (Players player : players) {
                        List <FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                        for (FifaStatsPlayer playerFifaStat : playerFifaStats) {
                            if (playerFifaStat.getFifa_version().equals(fifaV)) {
                                Optional<PlayersNodeDTO> playerNode = Pmn.findByMongoIdLight(player.get_id());
                                if(playerNode.isPresent()){
                                    Pmn.deletePlaysInTeamRelationToTeam(player.get_id(), id, fifaV);
                                }
                                playerFifaStat.getTeam().setTeam_name("DefaultTeam");
                                playerFifaStat.getTeam().setTeam_mongo_id("XXXXXXXXXXXX");
                                playerFifaStat.setClub_position("NA");
                                playerFifaStat.setLeague_name("DefaultLeague");
                                playerFifaStat.setClub_contract_valid_until_year(2999);
                                playerFifaStat.setClub_jersey_number(-1);
                                playerFifaStat.setLeague_level(-1);
                                PMr.save(player);
                                break;
                            }
                        }
                    }
                }
                List<Coaches> coaches = CMr.findByTeamMongoIdInTeams(id);
                if(!coaches.isEmpty()){
                    for(Coaches coach : coaches){
                        List<TeamObj> teamOb = coach.getTeam();
                        if(teamOb.isEmpty()){
                            continue;
                        }
                        for(TeamObj TEAM : teamOb){
                            if(TEAM.getFifa_version().equals(fifaV)){
                                TEAM.setFifa_version(-1);
                                TEAM.setTeam_mongo_id("XXXXXXXXXXXX");
                                TEAM.setTeam_name("DefaultTeam");
                                CMr.save(coach);
                                break;
                            }
                        }
                    }
                }
                return CompletableFuture.completedFuture("Completed");
            }
            else{
                throw new RuntimeErrorException(null, "Fifa stats not found with id: " + id);
            }
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
    }
}