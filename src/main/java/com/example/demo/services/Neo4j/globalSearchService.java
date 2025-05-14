package com.example.demo.services.Neo4j;
import org.neo4j.driver.types.Node;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.example.demo.projections.globalSearchResult;

import java.util.List;
import java.util.ArrayList;

@Service
public class globalSearchService {

    private final Neo4jClient neo4jClient;

    public globalSearchService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    public Page<globalSearchResult> globalSearch(String name,Pageable pageable) {
        
        String lowerName = name.toLowerCase();
        int skip = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();
        
        // Count total results first (for Page metadata)
        long total = neo4jClient.query(
            "MATCH (n) WHERE " +
            "(n.teamName IS NOT NULL AND toLower(n.teamName) CONTAINS $name) OR " +
            "(n.longName IS NOT NULL AND toLower(n.longName) CONTAINS $name) OR " +
            "(n.userName IS NOT NULL AND toLower(n.userName) CONTAINS $name) RETURN count(n) AS total"
        )
        .bind(lowerName).to("name")
        .fetchAs(Long.class)
        .one()
        .orElse(0L);

        Collection<globalSearchResult> results = neo4jClient.query("MATCH (n) WHERE "+
            "(n.teamName IS NOT NULL AND lower(n.teamName) CONTAINS $name) OR" +
            "(n.longName IS NOT NULL AND lower(n.longName) CONTAINS $name) OR" +
            "(n.userName IS NOT NULL AND lower(n.userName) CONTAINS $name) RETURN n SKIP $skip LIMIT $limit;")
        .bind(lowerName).to("name")
        .bind(skip).to("skip")
        .bind(limit).to("limit")
        .fetchAs(globalSearchResult.class)
        .mappedBy((typeSystem, record) -> {
            Node node = record.get("n").asNode();

            List<String> labels = StreamSupport
                    .stream(node.labels().spliterator(), false)
                    .collect(Collectors.toList());

            return new globalSearchResult(
                labels,
                node.asMap()
            );
        })
        .all();
        
        return new PageImpl<>(new ArrayList<>(results), pageable, total);
    }
}