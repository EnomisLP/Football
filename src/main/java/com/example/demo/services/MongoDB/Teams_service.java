package com.example.demo.services.MongoDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.management.RuntimeErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.models.MongoDB.FifaStatsTeam;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.repositories.MongoDB.Coaches_repository;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.requets.updateTeam;

import jakarta.transaction.Transactional;
@Service
public class Teams_service {
    
    private final Teams_repository TMr;
    private final Teams_node_rep Tmr;
    private static final Integer CURRENT_YEAR = 24;
    private final Players_repository PMr;
    private final Coaches_repository CMr;

    public Teams_service(Teams_repository tmr, Teams_node_rep TMR,
    Players_repository pmr, Coaches_repository cmr){
        this.TMr = tmr;
        this.Tmr = TMR;
        this.PMr = pmr;
        this.CMr = cmr;
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
                existingTeam.setGender(teamsDetails.getGender());
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
    //DELETE
    @Transactional
    public void deleteTeam(String id){
        Optional<Teams> team = TMr.findById(id);
        Optional<TeamsNode> teamsNode = Tmr.findByMongoId(id);
        if(team.isPresent()){
            TMr.deleteById(id);
        }
        if(teamsNode.isPresent()){
            Tmr.delete(teamsNode.get());
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