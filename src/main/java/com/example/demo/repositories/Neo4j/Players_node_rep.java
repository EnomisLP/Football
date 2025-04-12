package com.example.demo.repositories.Neo4j;

import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;


import com.example.demo.models.Neo4j.PlayersNode;
@Repository
public interface Players_node_rep  extends Neo4jRepository<PlayersNode, Long>{

    boolean existsByMongoId(String mongoId);
    Optional<PlayersNode> findByMongoId(String get_id);
    Optional<PlayersNode> findByPlayerId(Long playerId);
    List<PlayersNode> findAllByGender(String gender);
}
