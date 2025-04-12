package com.example.demo.repositories.Neo4j;


import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import org.springframework.stereotype.Repository;


import com.example.demo.models.Neo4j.TeamsNode;
@Repository
public interface Teams_node_rep extends Neo4jRepository<TeamsNode,Long>{
    boolean existsByMongoId(String valueOf);
    Optional<TeamsNode> findByMongoId(String get_id);
    Optional<TeamsNode> findByTeamId(Long teamId);
    List<TeamsNode> findAllByGender(String gender);

}
