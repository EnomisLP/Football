package com.example.demo.services.MongoDB;

import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.Articles;
import com.example.demo.models.Neo4j.ArticlesNode;
import com.example.demo.requets.updateArticle;
import com.example.demo.repositories.MongoDB.Articles_repository;

import jakarta.transaction.Transactional;

@Service
public class Articles_service {
    
    private final Articles_repository Ar;
    private final ArticlesNode articleNode;

    public Articles_service(Articles_repository ar, ArticlesNode articleNode) {
        this.Ar = ar;
        this.articleNode = articleNode;
    }

    //READ
    public Articles getArticle(String id){
        Optional<Articles> article = Ar.findById(id);
        if(article.isPresent()){
            return article.get();
        }
        else{
            throw new RuntimeErrorException(null, "Article not found with id: " + id);
        }
    }
    public Page<Articles> getAllArticles(PageRequest pageable) {
        return Ar.findAll(pageable);
        
    }
    
}
