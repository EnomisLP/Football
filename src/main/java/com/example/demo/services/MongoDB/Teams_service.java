package com.example.demo.services.MongoDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.management.RuntimeErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.FifaStatsTeam;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.relationships.manages_team;
import com.example.demo.repositories.MongoDB.Coaches_repository;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.requets.updateTeam;
import com.example.demo.requets.updateFifaTeam;

import jakarta.transaction.Transactional;
@Service
public class Teams_service {
    
    private Teams_repository TMr;
    private Teams_node_rep Tmr;
    private static final Integer CURRENT_YEAR = 24;
    private Players_repository PMr;
    private Coaches_repository CMr;
    private Coaches_node_rep CMn;

    public Teams_service(Teams_repository tmr, Teams_node_rep TMR,
    Players_repository pmr, Coaches_repository cmr, Coaches_node_rep CMn) {
        this.TMr = tmr;
        this.Tmr = TMR;
        this.PMr = pmr;
        this.CMr = cmr;
        this.CMn = CMn;
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
    @Transactional
    public Teams createTeam(Teams team){
        Teams teamM = TMr.save(team);
        TeamsNode newTeam = new TeamsNode();
        newTeam.setMongoId(teamM.get_id());
        newTeam.setTeamId(teamM.getTeam_id());
        newTeam.setTeamName(teamM.getTeam_name());
        newTeam.setGender(teamM.getGender());
        Tmr.save(newTeam);
        return teamM;
    }

    //UPDATE
    @Transactional
    public Teams updateTeam(String id, updateTeam teamsDetails) {
        Optional<Teams> optionalTeam = TMr.findById(id);
        if (optionalTeam.isPresent()) {
            Teams existingTeam = optionalTeam.get();
            Optional<TeamsNode> optionalTeamNode = Tmr.findByMongoId(existingTeam.get_id());
            if(optionalTeamNode.isPresent()){
                TeamsNode existingTeamNode = optionalTeamNode.get();
                
                //Updating attribute istances
                List<Players> players = PMr.findByClubTeamIdInFifaStats(existingTeam.getTeam_id());
                if(!players.isEmpty()){
                    for (Players player : players) {
                        List <FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                        for (FifaStatsPlayer playerFifaStat : playerFifaStats) {
                            playerFifaStat.setClub_team_id(teamsDetails.getTeam_id());
                            playerFifaStat.setClub_name(teamsDetails.getTeam_name());
                            PMr.save(player);
                        }
                    }
                }
                existingTeam.setGender(teamsDetails.getGender());
                existingTeam.setTeam_name(teamsDetails.getTeam_name());
                existingTeam.setTeam_id(teamsDetails.getTeam_id());
                
                existingTeamNode.setTeamName(teamsDetails.getTeam_name());
                existingTeamNode.setTeamId(teamsDetails.getTeam_id());
                existingTeamNode.setGender(teamsDetails.getGender());
               

                Tmr.save(existingTeamNode);
                // Save the updated team back to the repository
                return TMr.save(existingTeam);
            }
            else{
                throw new RuntimeException("Team with id: " + id+" not correctly mapped in Neo4j");
            }
        
        } else {
        throw new RuntimeException("Team not found with id: " + id);
        }
    }

    public Teams updateFifaTeam(String id, Integer fifaV, updateFifaTeam request) {
        Optional<Teams> optionalTeam = TMr.findById(id);
        if(optionalTeam.isPresent()){
            Teams existingTeam = optionalTeam.get();
            List<FifaStatsTeam> existingFifaStatsTeam = existingTeam.getFifaStats();
            for(FifaStatsTeam stat : existingFifaStatsTeam){
                if(stat.getFifa_version().equals(fifaV)){
                    if(!stat.getFifa_version().equals(request.getFifa_version())){
                        stat.setFifa_version(request.getFifa_version());

                        //Updating relationship in Neo4j
                        Optional<CoachesNode> optionalCoach = CMn.findByCoachId(stat.getCoach_id());
                        if(optionalCoach.isPresent()){
                            CoachesNode existingCoach = optionalCoach.get();
                            List<manages_team> relationships = existingCoach.getTeamMNodes();
                            for (manages_team relationship : relationships) {
                                if (relationship.getFifaV().equals(fifaV)) {
                                    relationship.setFifaV(request.getFifa_version());
                                    CMn.save(existingCoach);
                                    break;
                                }
                            }
                        }
                        else{
                            throw new RuntimeErrorException(null, "Coach not found with id: " + stat.getCoach_id());
                        }

                        //Updating attribute istances
                        List<Players> players = PMr.findByClubTeamIdInFifaStats(existingTeam.getTeam_id());
                        for(Players player : players){
                            List<FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                            for(FifaStatsPlayer playerFifaStat : playerFifaStats){
                                if(playerFifaStat.getFifa_version().equals(fifaV)){
                                    playerFifaStat.setLeague_name(request.getLeague_name());
                                    PMr.save(player);
                                    break;
                                }
                            }
                        }
                    }
                        stat.setLeague_id(request.getLeague_id());
                        stat.setLeague_level(request.getLeague_level());
                        stat.setNationality_id(request.getNationality_id());
                        stat.setNationality_name(request.getNationality_name());
                        stat.setHome_stadium(request.getHome_stadium());
                        stat.setOverall(request.getOverall());
                        stat.setClub_worth_eur(request.getClub_worth_eur());
                        stat.setAttack(request.getAttack());
                        stat.setDefence(request.getDefence());
                        stat.setMidfield(request.getMidfield());
                        stat.setOff_players_in_box(request.getOff_players_in_box());
                        stat.setOff_corners(request.getOff_corners());
                        stat.setOff_free_kicks(request.getOff_free_kicks());
                        break;
                }
                else{
                    throw new RuntimeErrorException(null, "Fifa stats not found with id: " + id);
                }
            }
            return TMr.save(existingTeam);
            
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
    }
    //DELETE
    @Transactional
    public void deleteTeam(String id){
        Optional<Teams> team = TMr.findById(id);
        Optional<TeamsNode> teamsNode = Tmr.findByMongoId(id);
        if(team.isPresent()){
            Teams existingTeam = team.get();

            //Deleting attribute istances
            List<Players> players = PMr.findByClubTeamIdInFifaStats(existingTeam.getTeam_id());
            if(!players.isEmpty()){
                for (Players player : players) {
                    List <FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                    for (FifaStatsPlayer playerFifaStat : playerFifaStats) {
                        playerFifaStat.setClub_team_id(null);
                        playerFifaStat.setClub_name(null);
                        playerFifaStat.setClub_position(null);
                        playerFifaStat.setLeague_name(null);
                        playerFifaStat.setClub_contract_valid_until_year(null);
                        playerFifaStat.setClub_jersey_number(null);
                        playerFifaStat.setLeague_level(null);
                        PMr.save(player);
                    }
                }
            }
            //deleting team in mongoDB
            TMr.deleteById(id);
        }
        //Deleting team in Neo4j
        if(teamsNode.isPresent()){
            Tmr.delete(teamsNode.get());
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
        
    }
    @Transactional
    public void deleteFifaTeam(String id, Integer fifaV){
        Optional<Teams> team = TMr.findById(id);
        Optional<TeamsNode> teamsNode = Tmr.findByMongoId(id);
        if(team.isPresent() && teamsNode.isPresent()){
            TeamsNode existingTeamNode = teamsNode.get();
            Teams existingTeam = team.get();
            List<FifaStatsTeam> existingFifaStats = existingTeam.getFifaStats();
            //removing relationship in Neo4j
            if(!existingFifaStats.isEmpty()) {
                for (FifaStatsTeam fifaStat : existingFifaStats) {
                    if (fifaStat.getFifa_version().equals(fifaV)) {
                        Optional<CoachesNode> optionalCoach = CMn.findByCoachId(fifaStat.getCoach_id());
                        if(optionalCoach.isPresent()){
                            CoachesNode existingCoach = optionalCoach.get();
                            manages_team relationship = CMn.findFifaVersionByTeamIdAndFifaV(existingTeamNode.getTeamId(), fifaV);
                            if (relationship != null) {
                                existingCoach.getTeamMNodes().remove(relationship);
                                CMn.save(existingCoach);
                                break;
                            }
                        }
                        else{
                            throw new RuntimeErrorException(null, "Coach not found with id: " + fifaStat.getCoach_id());
                        }
                    }
                }
                //Deleting fifa stats from MongoDB
                for (FifaStatsTeam fifaStat : existingFifaStats) {
                    if (fifaStat.getFifa_version().equals(fifaV)) {
                        existingTeam.getFifaStats().remove(fifaStat);
                        break;
                    }
                }
                TMr.save(existingTeam);

                //Deleting attribute istances
                
                List<Players> players = PMr.findByClubTeamIdInFifaStats(existingTeam.getTeam_id());
                if(!players.isEmpty()){
                    for (Players player : players) {
                        List <FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                        for (FifaStatsPlayer playerFifaStat : playerFifaStats) {
                            if (playerFifaStat.getFifa_version().equals(fifaV)) {
                                playerFifaStat.setClub_team_id(null);
                                playerFifaStat.setClub_name(null);
                                playerFifaStat.setClub_position(null);
                                playerFifaStat.setLeague_name(null);
                                PMr.save(player);
                                break;
                            }
                        }
                    }
                }
            }
            else{
                throw new RuntimeErrorException(null, "Fifa stats not found with id: " + id);
            }
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
    }

    //OPERATIONS TO MANAGE TEAM STATS
    public List<Players> showCurrentFormation(Long teamId) {
        Optional<Teams> optionalTeam = TMr.findByTeamId(teamId);
        List<Players> listToReturn = new ArrayList<>();
    
        if (optionalTeam.isPresent()) {
            Teams existingTeam = optionalTeam.get();
    
            // Get the current year's FIFA stats
            Optional<FifaStatsTeam> optionalFifa = existingTeam.getFifaStats().stream()
                    .filter(fifaStat -> fifaStat.getFifa_version().equals(CURRENT_YEAR))
                    .findFirst();
    
            if (optionalFifa.isPresent()) {
                FifaStatsTeam existingFifaStats = optionalFifa.get();
    
                // Retrieve each player by their ID and add them to the list
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getCaptain())
                        .orElseThrow(() -> new RuntimeException("Captain not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getShort_free_kick())
                        .orElseThrow(() -> new RuntimeException("Short free-kick taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getLong_free_kick())
                        .orElseThrow(() -> new RuntimeException("Long free-kick taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getLeft_short_free_kick())
                        .orElseThrow(() -> new RuntimeException("Left short free-kick taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getRight_short_free_kick())
                        .orElseThrow(() -> new RuntimeException("Right short free-kick taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getPenalties())
                        .orElseThrow(() -> new RuntimeException("Penalty taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getLeft_corner())
                        .orElseThrow(() -> new RuntimeException("Left corner taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getRight_corner())
                        .orElseThrow(() -> new RuntimeException("Right corner taker not found")));
    
                return listToReturn;
            } else {
                throw new RuntimeException("FIFA stats for current year not found for team: " + teamId);
            }
        } else {
            throw new RuntimeException("Team not found with ID: " + teamId);
        }
    }
    
    public List<Players> showSpecificFormation(Long teamId, Integer fifaV) {
        Optional<Teams> optionalTeam = TMr.findByTeamId(teamId);
        List<Players> listToReturn = new ArrayList<>();
    
        if (optionalTeam.isPresent()) {
            Teams existingTeam = optionalTeam.get();
    
            // Get the current year's FIFA stats
            Optional<FifaStatsTeam> optionalFifa = existingTeam.getFifaStats().stream()
                    .filter(fifaStat -> fifaStat.getFifa_version().equals(fifaV))
                    .findFirst();
    
            if (optionalFifa.isPresent()) {
                FifaStatsTeam existingFifaStats = optionalFifa.get();
    
                // Retrieve each player by their ID and add them to the list
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getCaptain())
                        .orElseThrow(() -> new RuntimeException("Captain not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getShort_free_kick())
                        .orElseThrow(() -> new RuntimeException("Short free-kick taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getLong_free_kick())
                        .orElseThrow(() -> new RuntimeException("Long free-kick taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getLeft_short_free_kick())
                        .orElseThrow(() -> new RuntimeException("Left short free-kick taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getRight_short_free_kick())
                        .orElseThrow(() -> new RuntimeException("Right short free-kick taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getPenalties())
                        .orElseThrow(() -> new RuntimeException("Penalty taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getLeft_corner())
                        .orElseThrow(() -> new RuntimeException("Left corner taker not found")));
    
                listToReturn.add(PMr.findByPlayerId(existingFifaStats.getRight_corner())
                        .orElseThrow(() -> new RuntimeException("Right corner taker not found")));
    
                return listToReturn;
            } else {
                throw new RuntimeException("FIFA stats for current year not found for team: " + teamId);
            }
        } else {
            throw new RuntimeException("Team not found with ID: " + teamId);
        }
    }

    public Coaches showCurrentCoach(Long teamId){
        Optional<Teams> optionalTeam = TMr.findByTeamId(teamId);
        if (optionalTeam.isPresent()) {
            Teams existingTeam = optionalTeam.get();
            Optional<FifaStatsTeam> optionalFifa = existingTeam.getFifaStats().stream()
                    .filter(fifaStat -> fifaStat.getFifa_version().equals(CURRENT_YEAR))
                    .findFirst();
    
            if (optionalFifa.isPresent()) {
                FifaStatsTeam existingFifaStats = optionalFifa.get();
                 return CMr.findByCoachId(existingFifaStats.getCoach_id())
                .orElseThrow(() -> new RuntimeException("Coach not found"));
            }
            else{
                throw new RuntimeException("FIFA stats for current year not found for team: " + teamId);
            }
        }
        else{
            throw new RuntimeException("Team not found with ID: " + teamId);
        }
    }

    public Coaches showSpecificCoach(Long teamId, Integer fifaV){
        Optional<Teams> optionalTeam = TMr.findByTeamId(teamId);
        if (optionalTeam.isPresent()) {
            Teams existingTeam = optionalTeam.get();
            Optional<FifaStatsTeam> optionalFifa = existingTeam.getFifaStats().stream()
                    .filter(fifaStat -> fifaStat.getFifa_version().equals(fifaV))
                    .findFirst();
    
            if (optionalFifa.isPresent()) {
                FifaStatsTeam existingFifaStats = optionalFifa.get();
                 return CMr.findByCoachId(existingFifaStats.getCoach_id())
                .orElseThrow(() -> new RuntimeException("Coach not found"));
            }
            else{
                throw new RuntimeException("FIFA stats for current year not found for team: " + teamId);
            }
        }
        else{
            throw new RuntimeException("Team not found with ID: " + teamId);
        }
    }
}