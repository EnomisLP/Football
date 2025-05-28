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
import com.example.demo.projections.CoachesNodeDTO;
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
    public CoachesNodeDTO getCoach(String mongoId) {
        return CMn.findByMongoIdLight(mongoId)
                .orElseThrow(() -> new RuntimeErrorException(null, "No Coach found with id: " + mongoId));
    }

    public Page<CoachesNodeDTO> getAllCoaches(PageRequest page, String gender){
        return CMn.findAllByGenderWithPaginationLight(gender, page);
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
        Optional<CoachesNodeDTO> coach = CMn.findByMongoIdLight(mongoId);
        if (coach.isPresent()) {
            CMn.deleteCoachByMongoIdLight(mongoId);
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
                CMn.createManagesRelationToTeam(existingCoach.get_id(), teamNode.getMongoId(), fifaStat.getFifa_version());
                
                counter++;
            }
        }
        return "Number of Relationships Created: " + counter;
    }
    
    public List<TeamsNodeDTO> showTrainedHistory(String coachMongoId){
        Optional<CoachesNodeDTO> optionalCoach = CMn.findByMongoIdLight(coachMongoId);
        if(optionalCoach.isPresent()){
            return CMn.findHistoryTrained(coachMongoId);
        }
        else{
            throw new RuntimeErrorException(null, "Coach with id:" + coachMongoId + "not found");
        }
    }

    public TeamsNodeDTO showCurrentTeam(String coachMongoId){
        Optional<CoachesNodeDTO> optionalCoach = CMn.findByMongoIdLight(coachMongoId);
        if(optionalCoach.isPresent()){
           return CMn.findTeam(coachMongoId, CURRENT_YEAR);
        }
        else{
            throw new RuntimeErrorException(null, "Coach with id:" + coachMongoId + " not found or team not found for this year");
        }
    }

    public TeamsNodeDTO showSpecificTeam(String coachMongoId, Integer fifaV){
        Optional<CoachesNodeDTO> optionalCoach = CMn.findByMongoIdLight(coachMongoId);
        
        if(optionalCoach.isPresent()){
            return CMn.findTeam(coachMongoId, fifaV);
        }
        else{
            throw new RuntimeErrorException(null, "Coach with id:" + coachMongoId + " not found or team not found for this year");
        }
    }
}
