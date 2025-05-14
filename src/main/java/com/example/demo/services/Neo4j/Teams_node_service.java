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
import com.example.demo.models.MongoDB.Teams;

import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.relationships.manages_team;
import com.example.demo.relationships.plays_in_team;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Teams_node_rep;

import jakarta.transaction.Transactional;

@Service
public class Teams_node_service {

    private final static Integer CURRENT_YEAR = 24;
    private final Teams_node_rep TMn;
    private final Teams_repository TMr;
    @Autowired
    private Neo4jClient neo4jClient;
    public Teams_node_service(Teams_node_rep tmn, Teams_repository tmr) {
        this.TMn = tmn;
        this.TMr = tmr;
   
    }

    // READ
    public TeamsNode getTeams(String mongoId) {
        Optional<TeamsNode> optionalTeam = TMn.findByMongoId(mongoId);
        if (optionalTeam.isPresent()) {
            return optionalTeam.get();
        } else {
            throw new RuntimeErrorException(null, "Team not found with id: " + mongoId);
        }
    }

       
    public Page<TeamsNode> getAllTeams(String gender, PageRequest page){
        return TMn.findAllByGenderWithPagination(gender, page);
    }


    private void ensureTeamNodeIndexes() {
        neo4jClient.query("""
            CREATE INDEX mongoId IF NOT EXISTS FOR (t:TeamsNode) ON (t.mongoId)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX teamName IF NOT EXISTS FOR (t:TeamsNode) ON (t.teamName)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX gender IF NOT EXISTS FOR (t:TeamsNode) ON (t.gender)
        """).run();
    }
    @Transactional
    public String doMapAllTheNodes(){
        List<Teams> Allteams = TMr.findAll();
        List<TeamsNode> nodeToInsert = new ArrayList<>();
        System.out.println("Teams found :" + Allteams.size());
        for (Teams team : Allteams) {
            // Check if the team node already exists in Neo4j
            if (TMn.existsByMongoId(team.get_id())) {
                continue;
            }
            

            // Create a new Neo4j node for the team
            TeamsNode teamsNode = new TeamsNode();
            teamsNode.setMongoId(team.get_id());
            teamsNode.setTeamName(team.getTeam_name());
            teamsNode.setGender(team.getGender());
            nodeToInsert.add(teamsNode);
        }
        TMn.saveAll(nodeToInsert);
        return "The amount of TeamsNode created are: " + nodeToInsert.size();
    }
    public String MapAllTheNodes() {
        // Ensure indexes are created
        ensureTeamNodeIndexes();
        return doMapAllTheNodes();
    }

    // DELETE
    public void deleteTeam(String mongoId) {
        Optional<TeamsNode> optionalTeam = TMn.findByMongoId(mongoId);
        if (optionalTeam.isPresent()) {
            TMn.delete(optionalTeam.get());
        } else {
            throw new RuntimeErrorException(null, "Team not found with id: " + mongoId);
        }
    }

    public List<plays_in_team> showCurrentFormation(String mongoId){
        Optional<TeamsNode> optionalTeamsNode = TMn.findByMongoId(mongoId);
        if(optionalTeamsNode.isPresent()){
            return TMn.findFormation(mongoId, CURRENT_YEAR);
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id" + mongoId);
        }
    }

    public List<plays_in_team> showSpecificFormation(String mongoId, Integer fifaV){
        Optional<TeamsNode> optionalTeamsNode = TMn.findByMongoId(mongoId);
        if(optionalTeamsNode.isPresent()){
            return TMn.findFormation(mongoId, fifaV);
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id" + mongoId);
        }
    }

    public manages_team showCurrentCoach(String mongoId){
         Optional<TeamsNode> optionalTeamsNode = TMn.findByMongoId(mongoId);
        if(optionalTeamsNode.isPresent()){
            return TMn.findCoach(mongoId, CURRENT_YEAR);
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with mongoId" + mongoId);
        }
    }

    public manages_team showSpecificCoach(String mongoId, Integer fifaV){
         Optional<TeamsNode> optionalTeamsNode = TMn.findByMongoId(mongoId);
        if(optionalTeamsNode.isPresent()){
            return TMn.findCoach(mongoId, fifaV);
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with name" + mongoId);
        }
    }
}
