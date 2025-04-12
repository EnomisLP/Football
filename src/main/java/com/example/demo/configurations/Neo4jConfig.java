package com.example.demo.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@Configuration
@EnableNeo4jRepositories(basePackages = "com.example.demo.repositories.Neo4j")
public class Neo4jConfig {
}
