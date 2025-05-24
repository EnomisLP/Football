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

import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.models.MongoDB.FifaStatsTeam;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.projections.TeamsNodeDTO;
import com.example.demo.relationships.manages_team;
import com.example.demo.repositories.MongoDB.Coaches_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;

import jakarta.transaction.Transactional;

@Service
public class Coaches_node_service {

    private final Coaches_node_rep CMn;
    private final Coaches_repository Cmr;
    private final Teams_node_rep TMn;
    private final Teams_repository TMr;
    private static final Integer CURRENT_YEAR = 24;

    @Autowired
    private Neo4jClient neo4jClient;

    public Coaches_node_service(Coaches_node_rep cmn, Coaches_repository CMR,
                                  Teams_node_rep TMN, Teams_repository TMR) {
        this.CMn = cmn;
        this.Cmr = CMR;
        this.TMn = TMN;
        this.TMr = TMR;
    }

    // READ
    public CoachesNode getCoach(String mongoId) {
        Optional<CoachesNode> optionalCoach = CMn.findByMongoId(mongoId);
        if (optionalCoach.isPresent()) {
            return optionalCoach.get();
        } else {
            throw new RuntimeErrorException(null, "Coach not found with Id: " + mongoId);
        }
    }

    public Page<CoachesNode> getAllCoaches(PageRequest page, String gender){
        return CMn.findAllByGenderWithPagination(gender, page);
    }


    private void ensureCoachNodeIndexes() {
        neo4jClient.query("""
            CREATE INDEX mongoId IF NOT EXISTS FOR (c:CoachesNode) ON (c.mongoId)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX longName IF NOT EXISTS FOR (c:CoachesNode) ON (c.longName)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX gender IF NOT EXISTS FOR (c:CoachesNode) ON (c.gender)
        """).run();
    }

    @Transactional
    public String doMapAllTheNodes(){
        List<Coaches> Allcoaches = Cmr.findAll();
        List<CoachesNode> nodeToInsert = new ArrayList<>();
        System.out.println("Coaches found :" + Allcoaches.size());
        for (Coaches coach : Allcoaches) {
            // Check if the coach node already exists in Neo4j
            if (CMn.existsByMongoId(coach.get_id())) {
                continue;
            }
            // Create a new Neo4j node for the coach
            CoachesNode coachNode = new CoachesNode();
            coachNode.setMongoId(coach.get_id());
            coachNode.setLongName(coach.getLong_name());
            coachNode.setNationalityName(coach.getNationality_name());
            coachNode.setGender(coach.getGender());
            nodeToInsert.add(coachNode);
            
        }
        CMn.saveAll(nodeToInsert);
        return "The amount of CoachesNode created are: " + nodeToInsert.size();
    }
    public String MapAllTheNodes() {
        // Ensure indexes are created before mapping nodes
        ensureCoachNodeIndexes();
        return doMapAllTheNodes();
        
    }
    // DELETE
    public void deleteCoach(String mongoId) {
        Optional<CoachesNode> coach = CMn.findByMongoId(mongoId);
        if (coach.isPresent()) {
            CoachesNode existingCoach = coach.get();
            // Delete the relationships first
            existingCoach.getTeamMNodes().clear();
            CMn.delete(existingCoach);
        } else {
            throw new RuntimeErrorException(null, "No Coach found with id: " + mongoId);
        }
    }

    //OPERATIONS TO MANAGE TEAMS MANAGED BY A COACH


    public String MapAllManagesTeam(String gender) {
        int counter = 0;
        List<TeamsNodeDTO> listOfTeams = TMn.findAllLightByGender(gender);
    
        for (TeamsNodeDTO teamNode : listOfTeams) {
            if(teamNode.getMongoId() == null){
                System.err.println("Team node with id: " + teamNode.getMongoId() + " has no mongoId");
                continue;
            }
            Optional<Teams> optionalTeam = TMr.findById(teamNode.getMongoId());
            if (optionalTeam.isEmpty()) {
                System.err.println("Team with id: " + teamNode.getMongoId() + " not correctly mapped in MongoDB");
                continue;
            }
            Teams existingTeam = optionalTeam.get();
    
            for (FifaStatsTeam fifaStat : existingTeam.getFifaStats()) {
                String mongoId = fifaStat.getCoach().getCoach_mongo_id();
                if(mongoId == null){
                    System.err.println("Coach mongo ID is null for coach: " + fifaStat.getCoach().getCoach_name());
                    continue;
                }
                Optional<Coaches> optionalCoach = Cmr.findById(mongoId);
                if (optionalCoach.isEmpty()) {
                    System.err.println("Coach with id: " + mongoId + " not correctly mapped in MongoDB");
                    continue;
                }
    
                Coaches existingCoach = optionalCoach.get();
                Optional<CoachesNode> optionalCoachNode = CMn.findByMongoId(existingCoach.get_id());
                Optional<TeamsNode> optionalTeamNode = TMn.findByMongoId(teamNode.getMongoId());
                if (optionalCoachNode.isEmpty() || optionalTeamNode.isEmpty()) {
                    System.err.println("Team node with id: " + teamNode.getMongoId() + " not correctly mapped in Neo4j or Coach node with id: " + existingCoach.get_id());
                    continue;
                }
                CMn.createManagesRelationToTeam(optionalCoachNode.get().getMongoId(), optionalTeamNode.get().getMongoId(), fifaStat.getFifa_version());
                
                counter++;
            }
        }
        return "Number of Relationships Created: " + counter;
    }
    
    public List<manages_team> showTrainedHistory(String coachMongoId){
        Optional<CoachesNode> optionalCoach = CMn.findByMongoId(coachMongoId);
        if(optionalCoach.isPresent()){
            CoachesNode existingCoachNode = optionalCoach.get();
            return existingCoachNode.getTeamMNodes();
        }
        else{
            throw new RuntimeErrorException(null, "Coach with id:" + coachMongoId + "not found");
        }
    }

    public manages_team showCurrentTeam(String coachMongoId){
        Optional<CoachesNode> optionalCoach = CMn.findByMongoId(coachMongoId);
        Optional<manages_team> optionalRelationship = optionalCoach.flatMap(coach -> coach.getTeamMNodes().stream()
        .filter(team -> team.getFifaV().equals(CURRENT_YEAR)).findFirst());
        if(optionalCoach.isPresent() && optionalRelationship.isPresent()){
            manages_team existingTeam = optionalRelationship.get();
            return existingTeam;
        }
        else{
            throw new RuntimeErrorException(null, "Coach with id:" + coachMongoId + " not found or team not found for this year");
        }
    }

    public manages_team showSpecificTeam(String coachMongoId, Integer fifaV){
        Optional<CoachesNode> optionalCoach = CMn.findByMongoId(coachMongoId);
        Optional<manages_team> optionalRelationship = optionalCoach.flatMap(coach -> coach.getTeamMNodes().stream()
        .filter(team -> team.getFifaV().equals(fifaV)).findFirst());
        if(optionalCoach.isPresent() && optionalRelationship.isPresent()){
            manages_team existingTeam = optionalRelationship.get();
            return existingTeam;
        }
        else{
            throw new RuntimeErrorException(null, "Coach with id:" + coachMongoId + " not found or team not found for this year");
        }
    }
}
