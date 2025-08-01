package com.example.demo.repositories.Neo4j;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.DTO.PlayersNodeDTO;
import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.models.Neo4j.PlayersNode;
@Repository
public interface Players_node_rep  extends Neo4jRepository<PlayersNode, Long>{

    boolean existsByMongoId(String mongoId);
    Optional<PlayersNode> findByMongoId(String get_id);

    @Query("MATCH (p:PlayersNode {mongoId: $mongoId}) " +
            "RETURN p.mongoId AS mongoId, p.longName AS longName, p.gender AS gender, p.nationalityName AS nationality_name")
    Optional<PlayersNodeDTO> findByMongoIdLight(String mongoId);
    @Query(
    value = "MATCH (p:PlayersNode {gender: $gender}) " +
            "RETURN  p.mongoId AS mongoId, p.longName AS longName, p.gender AS gender, p.nationalityName AS nationality_name SKIP $skip LIMIT $limit",
    countQuery = "MATCH (p:PlayersNode {gender: $gender}) RETURN count( p)")
    Page<PlayersNodeDTO> findAllByGenderWithPagination(String gender, Pageable page);

    List<PlayersNode> findAllByGender(String gender);
    Optional<PlayersNode> findByLongName(String string);

    @Query("MATCH (n:PlayersNode {gender : $gender}) RETURN n.mongoId AS mongoId, n.longName AS longName, n.gender AS gender")
    List<PlayersNodeDTO> findAllLightByGender(String gender);

    @Query("MATCH (p:PlayersNode {mongoId: $mongoId}), (t:TeamsNode {mongoId: $teamId}) " +
    "MERGE (p)-[r:PLAYS_IN_TEAM {fifaVersion: $fifaV}]->(t) ")
    void createPlaysInTeamRelationToTeam(String mongoId, String teamId, Integer fifaV);
    @Query("MATCH (p:PlayersNode {mongoId: $mongoId})-[r:PLAYS_IN_TEAM]->(t:TeamsNode {mongoId: $teamId}) " +
    "WHERE r.fifaVersion = $fifaV " +
    "DELETE r")
    void deletePlaysInTeamRelationToTeam(String mongoId, String teamId, Integer fifaV);
    @Query("MATCH (p:PlayersNode {mongoId: $mongoId}) " +
    "DETACH DELETE p")
    void deletePlayerByMongoIdLight(String mongoId);

    @Query("MATCH (p:PlayersNode) " +
           "WHERE p.mongoId = $mongoId " +
           "SET p.longName = $longName, p.gender = $gender")
    void updatePlayerAttributes(String mongoId, String longName, String gender);

    @Query( "MATCH (p:PlayersNode {mongoId: $mongoId})-[r:PLAYS_IN_TEAM]->(t:TeamsNode) "+
    "WHERE r.fifaVersion = $fifaV "+
    "RETURN t.mongoId AS mongoId, t.longName AS longName, t.gender AS gender, r.fifaVersion AS fifaVersion")
    TeamsNodeDTO findTeam(String mongoId, Integer fifaV);
    
    @Query("MATCH (u:UsersNode {userName: $username})-[r:LIKES]->(p:PlayersNode {mongoId: $mongoId})" +
       "RETURN COUNT(r) > 0 AS relationshipExists")
    boolean checkLike(String mongoId, String username);

  @Query("MATCH (u:UsersNode)-[r:LIKES]->(a:PlayersNode {mongoId : $id})" +
       "RETURN COUNT(r)")
    Integer countLike(String id);
}
