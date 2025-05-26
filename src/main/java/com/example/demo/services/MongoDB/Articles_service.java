package com.example.demo.services.MongoDB;

import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.Articles;
import com.example.demo.repositories.MongoDB.Articles_repository;



@Service
public class Articles_service {
    
    private final Articles_repository Ar;
    private final Articles_node_rep Anr;
    

    public Articles_service(Articles_repository ar,Articles_node_rep anr ) {
        this.Ar = ar;
        this.Anr = anr;
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
    
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> deleteArticleById(String articleId){
        Optional<Articles> optionalArticle = Ar.findById(articleId);
        Optional<ArticlesNode> optionalArticleNode = Anr.findByMongoId(articleId);
        if(optionalArticle.isPresent() && optionalArticleNode.isPresent()){
            Articles existingArticle = optionalArticle.get();
            ArticlesNode existingArticleNode = optionalArticleNode.get();
            if(existingArticle.getAuthor().equals(existingUser.getUsername()) && existingArticleNode.getAuthor().equals(existingUserNode.getUserName())){
                Ar.deleteById(articleId);
                AR.delete(existingArticleNode);
                UNr.save(existingUserNode);
                return CompletableFuture.completedFuture("Article deleted correctly!");
            }
            else{
                throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
            }
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
        }
    }
    
}
