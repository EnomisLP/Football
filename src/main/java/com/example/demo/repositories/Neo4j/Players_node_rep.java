package com.example.demo.repositories.Neo4j;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import com.example.demo.models.Neo4j.PlayersNode;
import com.example.demo.projections.PlayersNodeDTO;
@Repository
public interface Players_node_rep  extends Neo4jRepository<PlayersNode, Long>{

    boolean existsByMongoId(String mongoId);
    Optional<PlayersNode> findByMongoId(String get_id);
    
    @Query(
    value = "MATCH (p:PlayersNode {gender: $gender}) RETURN p SKIP $skip LIMIT $limit",
    countQuery = "MATCH (p:PlayersNode {gender: $gender}) RETURN count(p)"
)
    Page<PlayersNode> findAllByGenderWithPagination(String gender, Pageable page);
    
    List<PlayersNode> findAllByGender(String gender);
    Optional<PlayersNode> findByLongName(String string);

    @Query("MATCH (n:PlayersNode {gender : $gender}) RETURN n.mongoId AS mongoId, n.longName AS longName, n.gender AS gender")
    List<PlayersNodeDTO> findAllLightByGender(String gender);

    @Query("MATCH (p:PlayersNode {mongoId: $mongoId}), (t:TeamsNode {mongoId: $teamId}) " +
    "MERGE (p)-[r:PLAYS_IN_TEAM {fifaVersion: $fifaV}]->(t) ")
    void createPlaysInTeamRelationToTeam(String mongoId, String teamId, Integer fifaV);

    @Query("MATCH (p:PlayersNode {mongoId: $mongoId}) " +
    "DETACH DELETE p")
    void deletePlayerByMongoId(String mongoId);
}
