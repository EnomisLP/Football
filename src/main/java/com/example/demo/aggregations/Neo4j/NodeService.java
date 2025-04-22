package com.example.demo.aggregations.Neo4j;

import java.util.Collection;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.example.demo.aggregations.DTO.TopTeamProjection;
import com.example.demo.models.Neo4j.TeamsNode;

@Service
public class NodeService {

    private final Neo4jClient neo4jClient;

    public NodeService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }
    // Method to find the most famous team based on the number of likes from users and the FIFA version
    public Collection<TopTeamProjection> findMostFamousTeam(int fifaVersion) {
        return this.neo4jClient.query("MATCH (p:PlayersNode)-[r:PLAYS_IN_TEAM]->(t:TeamsNode) " +
            "WHERE r.fifaVersion = $fifaVersion " +
            "WITH t, p " +
            "MATCH (u:UsersNode)-[l:LIKES_PLAYER]->(p) " +
            "WITH t, COUNT(l) AS fame " +
            "WITH t, SUM(fame) AS totalFame " +
            "RETURN t AS teamNode, totalFame " +
            "ORDER BY totalFame DESC " +
            "LIMIT 10")
                .bind(fifaVersion).to("fifaVersion")
                .fetchAs(TopTeamProjection.class)
                .mappedBy((typeSystem, record) -> {
                    var teamNode = record.get("teamNode");  
                    if (teamNode == null || teamNode.isNull()) {
                    return null;  
                    }
                    else{
                    TeamsNode team = new TeamsNode();
                    team.setMongoId(teamNode.get("mongoId").asString());
                    team.setTeamId(teamNode.get("teamId").asLong());
                    team.setTeamName(teamNode.get("teamName").asString());
                    team.setGender(teamNode.get("gender").asString());
                    Integer totalFame = record.get("totalFame").asInt();
                    return new TopTeamProjection(team, totalFame);
                    }
                })
                .all();
    }
}

