package com.example.demo.services.Neo4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;


import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.models.MongoDB.FifaStatsTeam;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.relationships.manages_team;
import com.example.demo.repositories.MongoDB.Coaches_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;

@Service
public class Coaches_node_service {

    private final Coaches_node_rep CMn;
    private final Coaches_repository Cmr;
    private final Teams_node_rep TMn;
    private final Teams_repository TMr;
    private static final Integer CURRENT_YEAR = 24;

    public Coaches_node_service(Coaches_node_rep cmn, Coaches_repository CMR,
                                  Teams_node_rep TMN, Teams_repository TMR) {
        this.CMn = cmn;
        this.Cmr = CMR;
        this.TMn = TMN;
        this.TMr = TMR;
    }

    // READ
    public CoachesNode getCoach(Integer coach_id) {
        Optional<CoachesNode> optionalCoach = CMn.findByCoachId(coach_id);
        if (optionalCoach.isPresent()) {
            return optionalCoach.get();
        } else {
            throw new RuntimeErrorException(null, "Coach not found with id: " + coach_id);
        }
    }

      public List<CoachesNode> getAllCoaches(String gender){
        return CMn.findAllByGender(gender);
    }

    // UPDATE
    public CoachesNode updateCoach(Long id, CoachesNode coachDetails) {
        Optional<CoachesNode> optionalCoachNode = CMn.findById(id);
        Optional<Coaches> optionalCoach = optionalCoachNode.flatMap(c -> Cmr.findById(c.getMongoId()));
        if (optionalCoachNode.isPresent() && optionalCoach.isPresent()) {
            CoachesNode existingCoachNode = optionalCoachNode.get();
            Coaches existingCoach = optionalCoach.get();

            // Update Neo4j node
            existingCoachNode.setCoachId(coachDetails.getCoachId());
            existingCoachNode.setLongName(coachDetails.getLongName());
            existingCoachNode.setNationalityName(coachDetails.getNationalityName());
            existingCoachNode.setGender(coachDetails.getGender());

            // Update MongoDB document
            existingCoach.setCoach_id(coachDetails.getCoachId());
            existingCoach.setLong_name(coachDetails.getLongName());
            existingCoach.setNationality_name(coachDetails.getNationalityName());
            existingCoach.setGender(coachDetails.getGender());

            Cmr.save(existingCoach);
            return CMn.save(existingCoachNode);
        } else {
            throw new RuntimeErrorException(null, "Coach with id: " + id + " not correctly mapped on MongoDB or Neo4j");
        }
    }

    public String MapAllTheNodes() {
        List<Coaches> Allcoaches = Cmr.findAll();
        List<CoachesNode> nodeToInsert = new ArrayList<>();
        System.out.println("Coaches found :" + Allcoaches.size());
        for (Coaches coach : Allcoaches) {
            // Check if the coach node already exists in Neo4j
            if (CMn.existsByMongoId(String.valueOf(coach.get_id()))) {
                continue;
            }
            // Create a new Neo4j node for the coach
            CoachesNode coachNode = new CoachesNode();
            coachNode.setMongoId(coach.get_id());
            coachNode.setCoachId(coach.getCoach_id());
            coachNode.setLongName(coach.getLong_name());
            coachNode.setNationalityName(coach.getNationality_name());
            coachNode.setGender(coach.getGender());
            nodeToInsert.add(coachNode);
            
        }
        CMn.saveAll(nodeToInsert);
        return "The amount of CoachesNode created are: " + nodeToInsert.size();
    }
    // DELETE
    public void deleteCoach(Long id) {
        Optional<CoachesNode> coach = CMn.findById(id);
        if (coach.isPresent()) {
            CoachesNode existingCoach = coach.get();
            existingCoach.getTeamMNodes().clear();
            CMn.save(existingCoach);
            CMn.deleteById(id);
        } else {
            throw new RuntimeErrorException(null, "No Coach found with id: " + id);
        }
    }

    //OPERATIONS TO MANAGE TEAMS MANAGED BY A COACH


    public String MapAllManagesTeam(String gender) {
        int counter = 0;
        List<TeamsNode> listOfTeams = TMn.findAllByGender(gender);
    
        for (TeamsNode teamNode : listOfTeams) {
            Optional<Teams> optionalTeam = TMr.findById(teamNode.getMongoId());
            if (optionalTeam.isEmpty()) {
                System.err.println("Team with id: " + teamNode.getMongoId() + " not correctly mapped in MongoDB");
                continue;
            }
    
            Teams existingTeam = optionalTeam.get();
            //List<FifaStatsTeam> fifaStats = existingTeam.getFifaStatsT();
            //if (fifaStats.isEmpty()) {
            //    System.err.println("Team with id: " + teamNode.getMongoId() + " without stats");
            //    continue;
           // }
    
            for (FifaStatsTeam fifaStat : existingTeam.getFifaStats()) {
                Optional<Coaches> optionalCoach = Cmr.findByCoachId(fifaStat.getCoach_id());
                if (optionalCoach.isEmpty()) {
                    System.err.println("Coach with id: " + fifaStat.getCoach_id() + " not correctly mapped in MongoDB");
                    continue;
                }
    
                Coaches existingCoach = optionalCoach.get();
                Optional<CoachesNode> optionalCoachNode = CMn.findByMongoId(existingCoach.get_id());
                if (optionalCoachNode.isEmpty()) {
                    System.err.println("Coach node with id: " + existingCoach.get_id() + " not correctly mapped in Neo4j");
                    continue;
                }
    
                CoachesNode existingCoachNode = optionalCoachNode.get();
    
                // Check for existing relationship
                boolean existingRelationship = existingCoachNode.getTeamMNodes().stream()
                        .anyMatch(rel -> rel.alreadyExist(teamNode, fifaStat.getFifa_version()));
    
                if (existingRelationship) {
                    System.err.println("Relationship already exists for Coach node with id: " + existingCoach.get_id() +
                                       " and Team node " + teamNode.getTeamId() +
                                       " in fifaVersion " + fifaStat.getFifa_version());
                    continue;
                }
    
                // Create new relationship
                manages_team newRelationship = new manages_team(teamNode, fifaStat.getFifa_version());
                existingCoachNode.getTeamMNodes().add(newRelationship);
                CMn.save(existingCoachNode);
                counter++;
            }
        }
        return "Number of Relationships Created: " + counter;
    }
    
    public List<manages_team> showTrainedHistory(Integer coachId){
        Optional<CoachesNode> optionalCoach = CMn.findByCoachId(coachId);
        if(optionalCoach.isPresent()){
            CoachesNode existingCoachNode = optionalCoach.get();
            return existingCoachNode.getTeamMNodes();
        }
        else{
            throw new RuntimeErrorException(null, "Coach with id:" + coachId + "not found");
        }
    }

    public manages_team showCurrentTeam(Integer coachId){
        Optional<CoachesNode> optionalCoach = CMn.findByCoachId(coachId);
        Optional<manages_team> optionalRelationship = optionalCoach.flatMap(coach -> coach.getTeamMNodes().stream()
        .filter(team -> team.getFifaV().equals(CURRENT_YEAR)).findFirst());
        if(optionalCoach.isPresent() && optionalRelationship.isPresent()){
            manages_team existingTeam = optionalRelationship.get();
            return existingTeam;
        }
        else{
            throw new RuntimeErrorException(null, "Coach with id:" + coachId + " not found or team not found for this year");
        }
    }

    public manages_team showSpecificTeam(Integer coachId, Integer fifaV){
        Optional<CoachesNode> optionalCoach = CMn.findByCoachId(coachId);
        Optional<manages_team> optionalRelationship = optionalCoach.flatMap(coach -> coach.getTeamMNodes().stream()
        .filter(team -> team.getFifaV().equals(fifaV)).findFirst());
        if(optionalCoach.isPresent() && optionalRelationship.isPresent()){
            manages_team existingTeam = optionalRelationship.get();
            return existingTeam;
        }
        else{
            throw new RuntimeErrorException(null, "Coach with id:" + coachId + " not found or team not found for this year");
        }
    }
}
