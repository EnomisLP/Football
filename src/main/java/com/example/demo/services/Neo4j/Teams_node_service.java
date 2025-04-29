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
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Teams_node_rep;

@Service
public class Teams_node_service {

   

    private final Teams_node_rep TMn;
    private final Teams_repository TMr;
    @Autowired
    private Neo4jClient neo4jClient;
    public Teams_node_service(Teams_node_rep tmn, Teams_repository tmr) {
        this.TMn = tmn;
        this.TMr = tmr;
   
    }

    // READ
    public TeamsNode getTeams(Long id) {
        Optional<TeamsNode> optionalTeam = TMn.findById(id);
        if (optionalTeam.isPresent()) {
            return optionalTeam.get();
        } else {
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
    }

       
    public Page<TeamsNode> getAllTeams(String gender, PageRequest page){
        return TMn.findAllByGenderWithPagination(gender, page);
    }

    // UPDATE
    public TeamsNode updateTeam(Long id, TeamsNode teamDetails) {
        Optional<TeamsNode> optionalTeamNode = TMn.findById(id);
        Optional<Teams> optionalTeam = optionalTeamNode.flatMap(t -> TMr.findById(t.getMongoId()));
        if (optionalTeamNode.isPresent() && optionalTeam.isPresent()) {
            TeamsNode existingTeamNode = optionalTeamNode.get();
            Teams existingTeam = optionalTeam.get();

            // Update Neo4j node
            existingTeamNode.setTeamId(teamDetails.getTeamId());
            existingTeamNode.setTeamName(teamDetails.getTeamName());
            existingTeamNode.setGender(teamDetails.getGender());

            // Update MongoDB document
            existingTeam.setTeam_id(teamDetails.getTeamId());
            existingTeam.setTeam_name(teamDetails.getTeamName());
            existingTeam.setGender(teamDetails.getGender());;

            TMr.save(existingTeam);
            return TMn.save(existingTeamNode);
        } else {
            throw new RuntimeErrorException(null, "Team with id: " + id + " not correctly mapped on MongoDB or Neo4j");
        }
    }

    private void ensureTeamNodeIndexes() {
        neo4jClient.query("""
            CREATE INDEX mongoId IF NOT EXISTS FOR (t:TeamsNode) ON (t.mongoId)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX teamId IF NOT EXISTS FOR (t:TeamsNode) ON (t.teamId)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX gender IF NOT EXISTS FOR (t:TeamsNode) ON (t.gender)
        """).run();
    }
    public String MapAllTheNodes() {
        // Ensure indexes are created
        ensureTeamNodeIndexes();
        List<Teams> Allteams = TMr.findAll();
        List<TeamsNode> nodeToInsert = new ArrayList<>();
        System.out.println("Teams found :" + Allteams.size());
        for (Teams team : Allteams) {
            // Check if the team node already exists in Neo4j
            if (TMn.existsByMongoId(String.valueOf(team.get_id()))) {
                continue;
            }
            

            // Create a new Neo4j node for the team
            TeamsNode teamsNode = new TeamsNode();
            teamsNode.setMongoId(team.get_id());
            teamsNode.setTeamId(team.getTeam_id());
            teamsNode.setTeamName(team.getTeam_name());
            teamsNode.setGender(team.getGender());
            nodeToInsert.add(teamsNode);
        }
        TMn.saveAll(nodeToInsert);
        return "The amount of TeamsNode created are: " + nodeToInsert.size();
    }

    // DELETE
    public void deleteTeam(Long id) {
        Optional<TeamsNode> optionalTeam = TMn.findById(id);
        if (optionalTeam.isPresent()) {
            TMn.deleteById(id);
        } else {
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
    }

}
