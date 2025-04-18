package com.example.demo.repositories.Neo4j;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;


import com.example.demo.models.Neo4j.TeamsNode;
@Repository
public interface Teams_node_rep extends Neo4jRepository<TeamsNode,Long>{
    boolean existsByMongoId(String valueOf);
    Optional<TeamsNode> findByMongoId(String get_id);
    Optional<TeamsNode> findByTeamId(Long teamId);
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

}
