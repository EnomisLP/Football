package com.example.demo.services.Neo4j;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.ArticlesNodeDTO;
import com.example.demo.DTO.UsersNodeDTO;
import com.example.demo.models.MongoDB.Articles;
import com.example.demo.models.Neo4j.ArticlesNode;
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

    public ArticlesNodeDTO getArticle(String article_id) {
        return Ar.findByMongoIdLight(article_id).orElseThrow(() -> new RuntimeException("Article not found with id: " + article_id));
        
    }

    public Page<ArticlesNodeDTO> getAllArticles(PageRequest page) {
        return Ar.findAllLightWithPagination(page);
    }

    //CALL THIS METHOD ONLY before mapping everithing
   public String MappAllWroteRelationships() {
    int count = 0;
    List<UsersNodeDTO> users = Un.findAllLight();
    List<ArticlesNodeDTO> articles = Ar.findAllLight();

    for (UsersNodeDTO user : users) {
        for(ArticlesNodeDTO article : articles) {
            if (user.getUserName().equals(article.getAuthor())) {
               Un.createWroteRelationToArticle(user.getUserName(), article.getMongoId());
                count++;
            }
        }

        
    }

    return "Mapped " + count + " wrote relationships successfully.";
}


    private void ensureArticlesNodeIndexes() {
        neo4jClient.query("""
            CREATE INDEX mongoId IF NOT EXISTS FOR (a:ArticlesNode) ON (a.mongoId)
        """).run();
        
    }
    @Transactional
    public String doMappAllArticles(){
        int count = 0;
        List<Articles> articles = ArM.findAll();
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
               Ar.save(articleNode);
                count++;
            }
            
        }
        return "Mapped " + count + " articles successfully.";
    }
    
    public String MappAllArticles(){
        ensureArticlesNodeIndexes();
        return doMappAllArticles();
    }
    public void deleteArticle(String mongoId){
        Optional<ArticlesNodeDTO> articleNode = Optional.ofNullable(Ar.findByMongoIdLight(mongoId).orElseThrow(() -> new RuntimeException("Article not found with id: " + mongoId)));
        Ar.deleteByMongoIdLight(articleNode.get().getMongoId());
    }
    public boolean checkLike(String articleId,String username){
        return this.Ar.checkLike(articleId,username);
    }
    public Integer countLike(String id){
        return this.Ar.countLike(id);
    }
}
