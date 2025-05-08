package com.example.demo.repositories.Neo4j;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.models.Neo4j.ArticlesNode;
import com.example.demo.models.Neo4j.UsersNode;

@Repository
public interface Articles_node_rep extends Neo4jRepository<ArticlesNode, Long> {

    Optional<ArticlesNode> findByMongoId(String articleId);

    Page<ArticlesNode> findAllByUsersNode(UsersNode existingUsersNode, PageRequest page);

    Page<ArticlesNode> findAll(PageRequest page);
    
}
