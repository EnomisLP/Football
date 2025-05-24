package com.example.demo.repositories.Neo4j;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.projections.CoachesNodeDTO;
import com.example.demo.projections.PlayersNodeDTO;
import com.example.demo.projections.TeamsNodeDTO;


@Repository
public interface Teams_node_rep extends Neo4jRepository<TeamsNode,Long>{
    boolean existsByMongoId(String valueOf);
    Optional<TeamsNode> findByMongoId(String get_id);
    
    @Query(
    value = "MATCH (t:TeamsNode {gender: $gender}) " +
            "OPTIONAL MATCH (t)<-[:PLAYS_IN_TEAM]-(p:PlayersNode) " +
            "OPTIONAL MATCH (t)<-[:HAS_IN_M_TEAM|HAS_IN_F_TEAM]-(u:UsersNode) " +
            "OPTIONAL MATCH (t)<-[:LIKES_TEAM]-(u2:UsersNode) " +
            "RETURN DISTINCT t, COLLECT(DISTINCT p) AS players, COLLECT(DISTINCT u) AS users, COLLECT(DISTINCT u2) AS likes",
    countQuery = "MATCH (t:TeamsNode {gender: $gender}) RETURN count(DISTINCT t)"
)
    Page<TeamsNode> findAllByGenderWithPagination(String gender, PageRequest pageRequest);
    List<TeamsNode> findAllByGender(String gender);
    @Query( "MATCH (t:TeamsNode {mongoId: $mongoId})<-[r:PLAYS_IN_TEAM]->(p:PlayersNode) "+
    "WHERE r.fifaVersion = $fifaV "+
    "RETURN { mongoId: p.mongoId, longName: p.longName, gender: p.gender} AS playerProjection")
    List<PlayersNodeDTO> findFormation(String mongoId, Integer fifaV);
    @Query( "MATCH (t:TeamsNode {mongoId: $mongoId})<-[r:MANAGES_TEAM]-(p:CoachesNode) "+
    "WHERE r.fifaVersion = $fifaV "+
    "RETURN { mongoId: p.mongoId, longName: p.longName, gender: p.gender} AS CoachesNodeProjection")
    CoachesNodeDTO findCoach(String mongoId, Integer fifaV);
    Optional<TeamsNode> findByLongName(String string);

    @Query("MATCH (n:TeamsNode {gender : $gender}) RETURN n.mongoId AS mongoId, n.longName AS longName, n.gender AS gender")
    List<TeamsNodeDTO> findAllLightByGender(String gender);

   
}
