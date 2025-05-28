package com.example.demo.aggregations.Neo4j;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;

import org.springframework.stereotype.Service;

import com.example.demo.aggregations.DTO.MostEngaggedPlayer;
import com.example.demo.aggregations.DTO.TopTeam;
import com.example.demo.aggregations.DTO.UserInterestDiversity;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.projections.PlayersNodeDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NodeService {

    private final Neo4jClient neo4jClient;

    public NodeService(@Autowired(required = false)Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }
     public void createNode() {
        if (neo4jClient == null) {
            log.warn("Neo4j is unavailable, skipping node creation.");
            return;
        }
    }
    // Method to find the most famous team based on the number of likes from users to players of specific FIFA version
    
    public Collection<TopTeam> findMostFamousTeam(int fifaVersion) {
        return this.neo4jClient.query(
            "MATCH (p:PlayersNode)-[r:PLAYS_IN_TEAM]->(t:TeamsNode) " +
            "WHERE r.fifaVersion = $fifaVersion " +
            "WITH t, COLLECT(DISTINCT p) AS teamPlayers " +
            "UNWIND teamPlayers AS player " +
            "OPTIONAL MATCH (u:UsersNode)-[l:LIKES]->(player) " +
            "WITH t, COUNT(l) AS totalFame " +  
            "RETURN t AS teamNode, totalFame " +
            "ORDER BY totalFame DESC " +
            "LIMIT 10")
            .bind(fifaVersion).to("fifaVersion")
            .fetchAs(TopTeam.class)
            .mappedBy((typeSystem, record) -> {
                var teamNode = record.get("teamNode");
                if (teamNode == null || teamNode.isNull()) {
                    return null;
                } else {
                    TeamsNode team = new TeamsNode();
                    team.setMongoId(teamNode.get("mongoId").asString());
                    team.setLongName(teamNode.get("longName").asString());
                    team.setGender(teamNode.get("gender").asString());
                    
                    var fameValue = record.get("totalFame");
                    Integer totalFame = fameValue.isNull() ? 0 : fameValue.asInt();
                    
                    return new TopTeam(team, totalFame);
                }
            })
            .all();
    }

    // Method to find the most engaged player based on the number of likes, teams, and followers
    // and the number of teams they are in
   
    public Collection<MostEngaggedPlayer> findMostEngagedPlayer() {
        return this.neo4jClient.query(
            "MATCH (p:PlayersNode) " +
            "WHERE p.mongoId IS NOT NULL " +
            "OPTIONAL MATCH (p)-[:PLAYS_IN_TEAM]->(t:TeamsNode) " +
            "OPTIONAL MATCH (u1:UsersNode)-[:LIKES]->(p) " +
            "OPTIONAL MATCH (u2:UsersNode)-[:HAS_IN_M_TEAM]->(p) " +
            "OPTIONAL MATCH (u3:UsersNode)-[:HAS_IN_F_TEAM]->(p) " +
            "WITH p, " +
            "COUNT(DISTINCT t) AS playsInTeamCount, " +
            "COUNT(DISTINCT u1) AS likes, " +
            "COUNT(DISTINCT u2) AS hasInMTeamCount, " +
            "COUNT(DISTINCT u3) AS hasInFTeamCount, " +
            "(COUNT(DISTINCT t) + COUNT(DISTINCT u1) + COUNT(DISTINCT u2) + COUNT(DISTINCT u3)) AS totalEngagement " +
            "RETURN p AS playerNode, totalEngagement " +
            "ORDER BY totalEngagement DESC " +
            "LIMIT 1 "
            )
            .fetchAs(MostEngaggedPlayer.class)
            .mappedBy((typeSystem, record) -> {
                var playerNode = record.get("playerNode");
                if (playerNode == null || playerNode.isNull()) {
                    return null;
                }
                else{
                    PlayersNodeDTO player = new PlayersNodeDTO();
                    player.setMongoId(playerNode.get("mongoId").asString());
                    player.setLongName(playerNode.get("longName").asString());
                    player.setGender(playerNode.get("gender").asString());
                   
                    Integer totalEngagement = record.get("totalEngagement").asInt();
                    return new MostEngaggedPlayer(player, totalEngagement);
                }
            })
            .all();

    }

    // Method to find the top 10 users with the most diverse interests based on the number of different entities they like
    // and the number of teams they have in their teams
    
    public Collection<UserInterestDiversity> findUserInterestDiversity() {
        return this.neo4jClient.query("MATCH (u:UsersNode)-[r:LIKES|HAS_IN_M_TEAM|HAS_IN_F_TEAM]->(entity) " +
            "WITH u, " +
            "COUNT(DISTINCT CASE WHEN TYPE(r) = 'LIKES' THEN entity END) AS likes, " +
            "COUNT(DISTINCT CASE WHEN TYPE(r) = 'HAS_IN_M_TEAM' THEN entity END) AS hasInMTeamCount, " +
            "COUNT(DISTINCT CASE WHEN TYPE(r) = 'HAS_IN_F_TEAM' THEN entity END) AS hasInFTeamCount " +
            "WITH u, " +
            "(likes + hasInMTeamCount + hasInFTeamCount) / 3.0 AS avgInterestCount " +
            "RETURN u.userName AS userName, avgInterestCount " +
            "ORDER BY avgInterestCount DESC " +
            "LIMIT 10")
            .fetchAs(UserInterestDiversity.class)
            .mappedBy((typeSystem, record) -> {
                String userName = record.get("userName").asString();
                Double avgInterestCount = record.get("avgInterestCount").asDouble();
                return new UserInterestDiversity(userName, avgInterestCount);
            })
            .all();
    }
    

}

