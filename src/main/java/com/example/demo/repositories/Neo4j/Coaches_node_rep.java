package com.example.demo.repositories.Neo4j;
import java.util.List;
import java.util.Optional;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;


import com.example.demo.models.Neo4j.CoachesNode;
@Repository
public interface Coaches_node_rep extends Neo4jRepository<CoachesNode,Long>{

    boolean existsByMongoId(String valueOf);
    Optional<CoachesNode> findByMongoId(String id);
    Optional<CoachesNode> findByCoachId(Integer coachId);
    List<CoachesNode> findAllByGender(String gender);
}
