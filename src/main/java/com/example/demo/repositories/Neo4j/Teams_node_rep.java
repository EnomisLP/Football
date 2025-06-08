package com.example.demo.repositories.Neo4j;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.DTO.CoachesNodeDTO;
import com.example.demo.DTO.PlayersNodeDTO;
import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.models.Neo4j.TeamsNode;


@Repository
public interface Teams_node_rep extends Neo4jRepository<TeamsNode,Long>{
    boolean existsByMongoId(String valueOf);
    Optional<TeamsNode> findByMongoId(String get_id);

    

    @Query("MATCH (t:TeamsNode {mongoId: $mongoId}) " +
           "RETURN t.mongoId AS mongoId, t.longName AS longName, t.gender AS gender")
    Optional<TeamsNodeDTO> findByMongoIdLight(String mongoId);
    @Query(

    value = "MATCH (t:TeamsNode {gender: $gender}) " +
            "RETURN t.longName AS longName, t.mongoId AS mongoId, t.gender AS gender SKIP $skip LIMIT $limit",
    countQuery = "MATCH (t:TeamsNode {gender: $gender}) RETURN count(DISTINCT t)")
    Page<TeamsNodeDTO> findAllByGenderWithPagination(String gender, Pageable pageable);

    List<TeamsNode> findAllByGender(String gender);
    @Query( "MATCH (t:TeamsNode {mongoId: $mongoId})<-[r:PLAYS_IN_TEAM]-(p:PlayersNode) "+
    "WHERE r.fifaVersion = $fifaV "+
    "RETURN p.mongoId AS mongoId, p.longName AS longName, p.gender AS gender,p.nationalityName As nationality_name, r.fifaVersion AS fifaV")
    List<PlayersNodeDTO> findFormation(String mongoId, Integer fifaV);
    @Query( "MATCH (t:TeamsNode {mongoId: $mongoId})<-[r:MANAGES_TEAM]-(p:CoachesNode) "+
    "WHERE r.fifaVersion = $fifaV "+
    "RETURN p.mongoId AS mongoId, p.longName AS longName, p.gender AS gender, r.fifaVersion AS fifaVersion")
    CoachesNodeDTO findCoach(String mongoId, Integer fifaV);
    Optional<TeamsNode> findByLongName(String string);

    @Query("MATCH (n:TeamsNode {gender : $gender}) RETURN n.mongoId AS mongoId, n.longName AS longName, n.gender AS gender")
    List<TeamsNodeDTO> findAllLightByGender(String gender);

    @Query("MATCH (t:TeamsNode {mongoId: $mongoId}) " +
           "DETACH DELETE t")
    void deleteByMongoIdLight(String mongoId);
    @Query("MATCH (t:TeamsNode {mongoId: $mongoId}) " +
           "SET t.longName = $newName ")
    void updateTeamName(String mongoId, String newName);

    @Query("MATCH (t:TeamsNode {mongoId: $mongoId}) " +
           "SET t.gender = $newGender ")
    void updateTeamGender(String mongoId, String newGender);
    
    @Query("MATCH (u:UsersNode {userName: $username})-[r:LIKES]->(t:TeamsNode {mongoId: $mongoId})" +
       "RETURN COUNT(r) > 0 AS relationshipExists")
    boolean checkLike(String mongoId, String username);

      @Query("MATCH (u:UsersNode)-[r:LIKES]->(a:TeamsNode {mongoId : $id})" +
       "RETURN COUNT(r)")
    Integer countLike(String id);
}
