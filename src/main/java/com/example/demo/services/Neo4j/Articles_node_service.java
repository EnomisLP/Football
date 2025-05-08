package com.example.demo.services.Neo4j;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.Articles;
import com.example.demo.models.Neo4j.ArticlesNode;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.repositories.MongoDB.Articles_repository;
import com.example.demo.repositories.Neo4j.Articles_node_rep;
import com.example.demo.repositories.Neo4j.Users_node_rep;

import jakarta.transaction.Transactional;

@Service
public class Articles_node_service {
    
    private final Articles_node_rep Ar;
    private final Users_node_rep Un;
    private final Articles_repository ArM;

    @Autowired
    private Neo4jClient neo4jClient;

    public Articles_node_service(Articles_node_rep Ar, Users_node_rep Un, Articles_repository ArM) {
        this.Ar = Ar;
        this.Un = Un;
        this.ArM = ArM;
    }

    public ArticlesNode getArticle(Long article_id) {
        return Ar.findById(article_id).orElseThrow(() -> new RuntimeException("Article not found with id: " + article_id));
    }

    public Page<ArticlesNode> getAllArticles(PageRequest page) {
        return Ar.findAll(page);
    }

    @Transactional
    public String MappAllWroteRelationships() {
        int count = 0;
        List<UsersNode> users = Un.findAll();
        List<ArticlesNode> articles = Ar.findAll();
        for (UsersNode user : users) {
            for (ArticlesNode article : articles) {
                if (user.getUserName().equals(article.getAuthor()) && !user.getArticlesNodes().contains(article)) {
                    user.getArticlesNodes().add(article);
                    count++;
                }
            }
        }
        Un.saveAll(users);
        return "Mapped " + count + " wrote relationships successfully.";
    }

    private void ensureArticlesNodeIndexes() {
        neo4jClient.query("""
            CREATE INDEX mongoId IF NOT EXISTS FOR (a:ArticlesNode) ON (a.mongoId)
        """).run();
        
        neo4jClient.query("""
            CREATE INDEX author IF NOT EXISTS FOR (a:ArticlesNode) ON (a.author)
        """).run();
    }
    @Transactional
    public String doMappAllArticles(){
        int count = 0;
        List<Articles> articles = ArM.findAll();
        List<ArticlesNode> articlesNodes = new ArrayList<>();
        for (Articles article : articles) {
            Optional<ArticlesNode> existingArticle = Ar.findByMongoId(article.get_id());
            if(existingArticle.isPresent()){
                continue;
            }
            else{
                ArticlesNode articleNode = new ArticlesNode();
                articleNode.setTitle(article.getTitle());
                articleNode.setAuthor(article.getAuthor());
                articleNode.setMongoId(article.get_id());
                articlesNodes.add(articleNode);
                count++;
            }
            
        }
        Ar.saveAll(articlesNodes);
        return "Mapped " + count + " articles successfully.";
    }
    
    public String MappAllArticles(){
        ensureArticlesNodeIndexes();
        return doMappAllArticles();
    }
}
