package com.example.demo.repositories.Neo4j;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.models.Neo4j.ArticlesNode;
import com.example.demo.projections.ArticlesNodeDTO;


@Repository
public interface Articles_node_rep extends Neo4jRepository<ArticlesNode, Long> {

    Optional<ArticlesNode> findByMongoId(String articleId);

    Page<ArticlesNode> findAllByAuthor(String userName, PageRequest page);

    @Query("MATCH (u:UsersNode {userName: })-[:WROTE]->(a:ArticlesNode {mongoId: }) " +
           "RETURN a")
    Optional<ArticlesNode> findOneByAuthorAndMongoId(String username, String articleId);

    @Query("MATCH (a:ArticlesNode {author: $author}) " +
           "RETURN a")
    List<ArticlesNode> findByAuthor(String userName);

@Query("MATCH (n:ArticlesNode) RETURN n.mongoId AS mongoId, n.title AS title, n.author AS author")
List<ArticlesNodeDTO> findAllLight();


}
