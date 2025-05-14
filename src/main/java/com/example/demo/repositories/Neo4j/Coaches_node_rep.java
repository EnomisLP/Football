package com.example.demo.repositories.Neo4j;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;


import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.relationships.manages_team;
@Repository
public interface Coaches_node_rep extends Neo4jRepository<CoachesNode,Long>{

    boolean existsByMongoId(String valueOf);
    Optional<CoachesNode> findByMongoId(String id);
    @Query(
        value = "MATCH (c:CoachesNode {gender: $gender}) " +
                "OPTIONAL MATCH (c)-[:MANAGES_TEAM]->(t:TeamsNode) " +
                "OPTIONAL MATCH (u:UsersNode)-[:LIKES_COACH]->(c) " +
                "RETURN DISTINCT c, COLLECT(DISTINCT t) AS teams, COLLECT(DISTINCT u) AS users",
        countQuery = "MATCH (c:CoachesNode {gender: $gender}) RETURN count(DISTINCT c)"
    )
    Page<CoachesNode> findAllByGenderWithPagination(String gender, PageRequest page);
    List<CoachesNode> findAllByGender(String gender);

    @Query("MATCH (c:CoachesNode) -[r:MANAGES_TEAM]-> (t:TeamsNode) " +
    "WHERE r.fifaVersion = $fifaV " +
    "AND t.teamName = $teamName " +
    "RETURN r")
    manages_team findFifaVersionByTeamNameAndFifaV(String teamName, Integer fifaV);
    
}
