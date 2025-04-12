package com.example.demo.services.MongoDB;

import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.Articles;

import com.example.demo.repositories.MongoDB.Articles_repository;

import jakarta.transaction.Transactional;

@Service
public class Articles_service {
    
    private final Articles_repository Ar;

    public Articles_service(Articles_repository ar){
        this.Ar = ar;
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
    
    //CREATE
    @Transactional
    public Articles createArticle(Articles article){
        return Ar.save(article);
    }

    //UPDATE
    @Transactional
    public Articles updateArticle(String id, Articles articleDetails){
        Optional<Articles> optionalArticle = Ar.findById(id);
        if(optionalArticle.isPresent()){
            Articles existingArticle = optionalArticle.get();
            existingArticle.setAuthor(articleDetails.getAuthor());
            existingArticle.setContent(articleDetails.getAuthor());
            existingArticle.setTitle(articleDetails.getTitle());
            existingArticle.setPublish_time(articleDetails.getPublish_time());
            return Ar.save(existingArticle);
        }
        else{
            throw new RuntimeErrorException(null, "Article not found with id: " + id);
        }
    }
    //DELETE
    @Transactional
    public void deleteArticle(String id){
        Optional<Articles> article = Ar.findById(id);
        if(article.isPresent()){
            Ar.deleteById(id);
        }
        else{
            throw new RuntimeErrorException(null, "Article not found with id: " + id);
        }
    }

    
}
