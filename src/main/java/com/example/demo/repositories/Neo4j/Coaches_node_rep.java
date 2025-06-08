package com.example.demo.repositories.Neo4j;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.DTO.CoachesNodeDTO;
import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.models.Neo4j.CoachesNode;

@Repository
public interface Coaches_node_rep extends Neo4jRepository<CoachesNode,Long>{

    boolean existsByMongoId(String valueOf);
    @Query("MATCH (c:CoachesNode {mongoId: $mongoId}) " +
           "RETURN c.mongoId AS mongoId, c.longName AS longName, c.gender AS gender")
    Optional<CoachesNodeDTO> findByMongoIdLight(String mongoId);

    Optional<CoachesNode> findByMongoId(String id);

    @Query(
        value = "MATCH (c:CoachesNode {gender: $gender}) " +
                "RETURN c.mongoId AS mongoId, c.longName AS longName, c.gender AS gender SKIP $skip LIMIT $limit",
        countQuery = "MATCH (c:CoachesNode {gender: $gender}) RETURN count(c)"
    )
    Page<CoachesNodeDTO> findAllByGenderWithPaginationLight(String gender, Pageable page);

    List<CoachesNode> findAllByGender(String gender);

    @Query("MATCH (c:CoachesNode) -[r:MANAGES_TEAM]-> (t:TeamsNode) " +
    "WHERE r.fifaVersion = $fifaV " +
    "AND t.mongoId = $mongoId " +
    "RETURN c.mongoId AS mongoId, c.longName AS long, c.gender AS gender, r.fifaVersion AS fifaVersion")
    Optional<CoachesNodeDTO> findFifaVersionByMongoIdAndFifaV(String mongoId, Integer fifaV);
    @Query("MATCH (c:CoachesNode) -[r:MANAGES_TEAM]-> (t:TeamsNode) " +
    "WHERE r.fifaVersion = $fifaV " +
    "AND c.mongoId = $mongoId " +
    "RETURN t.mongoId AS mongoId, t.longName AS long, t.gender AS gender, r.fifaVersion AS fifaVersion")
    Optional<TeamsNodeDTO> findTeamsbyFifaVAndMongoId(String mongoId, Integer fifaV);

    Optional<CoachesNode> findByLongName(String string);
    @Query("MATCH (c:CoachesNode {mongoId: $mongoId})-[r:MANAGES_TEAM]->(p:TeamsNode) "+
    "WHERE r.fifaVersion = $fifaV "+
    "RETURN p.mongoId AS mongoId, p.longName AS longName, p.gender AS gender, r.fifaVersion AS fifaVersion")
    TeamsNodeDTO findTeam(String mongoId, Integer fifaV);
    @Query("MATCH (c:CoachesNode {mongoId: $mongoId})-[r:MANAGES_TEAM]->(p:TeamsNode) " +
       "RETURN p.mongoId AS mongoId, p.longName AS longName, p.gender AS gender, r.fifaVersion AS fifaVersion")
    List<TeamsNodeDTO> findHistoryTrained(@Param("mongoId") String mongoId);

    @Query("MATCH (n:CoachesNode {gender : $gender}) RETURN n.mongoId AS mongoId, n.longName AS longName, n.gender AS gender")
    List<CoachesNodeDTO> findAllLightByGender( String gender);

    @Query("MATCH (c:CoachesNode {mongoId: $mongoId}), (t:TeamsNode {mongoId: $teamId}) " +
    "MERGE (c)-[r:MANAGES_TEAM {fifaVersion: $fifaV}]->(t) ")
    void createManagesRelationToTeam(String mongoId, String teamId, Integer fifaV);
    

    @Query("MATCH (c:CoachesNode {mongoId: $mongoId})-[r:MANAGES_TEAM]->(t:TeamsNode {mongoId: $teamId}) " +
    "WHERE r.fifaVersion = $fifaV " +
    "DELETE r")
    void deleteManagesRelationToTeam(String mongoId, String teamId, Integer fifaV);

     @Query("MATCH (c:CoachesNode {mongoId: $mongoId})-[r:MANAGES_TEAM]->(t:TeamsNode) " +
    "WHERE r.fifaVersion = $fifaV " +
    "DELETE r")
    void deleteOldManagesRelationToTeam(String mongoId, Integer fifaV);

    @Query("MATCH (c:CoachesNode {mongoId: $mongoId}) " +
    "DETACH DELETE c")
    void deleteCoachByMongoIdLight(String mongoId);

    @Query("MATCH (c:CoachesNode {mongoId: $mongoId}) " +
    "SET c.longName = $longName, c.gender = $gender, c.shortName = $shortName")
    void updateAttributesByMongoId(String mongoId, String shortName, String longName, String gender);
    
    @Query("MATCH (u:UsersNode {userName: $username})-[r:LIKES]->(t:CoachesNode {mongoId: $mongoId})" +
       "RETURN COUNT(r) > 0 AS relationshipExists")
    boolean checkLike(String mongoId, String username);
      @Query("MATCH (u:UsersNode)-[r:LIKES]->(a:CoachesNode {mongoId : $id})" +
       "RETURN COUNT(r)")
    Integer countLike(String id);
}
