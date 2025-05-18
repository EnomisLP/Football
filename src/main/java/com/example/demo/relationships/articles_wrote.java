package com.example.demo.relationships;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import com.example.demo.models.Neo4j.ArticlesNode;

@RelationshipProperties
public class articles_wrote {
    @RelationshipId
    @GeneratedValue
    private Long id;
    @TargetNode
    public ArticlesNode articlesNode;

    public articles_wrote(ArticlesNode articlesNode) {
        this.articlesNode = articlesNode;
    }
    public ArticlesNode getArticlesNode() {
        return articlesNode;
    }
    public boolean alreadyExists(ArticlesNode articlesNode) {
        if(this.articlesNode.equals(articlesNode)) {
            return true;
        } else {
            return false;
        }
    }
}
