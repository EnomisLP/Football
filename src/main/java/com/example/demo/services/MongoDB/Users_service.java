package com.example.demo.services.MongoDB;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.models.MongoDB.Articles;
import com.example.demo.models.MongoDB.ROLES;
import com.example.demo.models.MongoDB.Users;
import com.example.demo.models.Neo4j.ArticlesNode;
import com.example.demo.models.Neo4j.UsersNode;
import com.example.demo.repositories.MongoDB.Articles_repository;
import com.example.demo.repositories.MongoDB.Users_repository;
import com.example.demo.repositories.Neo4j.Articles_node_rep;
import com.example.demo.repositories.Neo4j.Users_node_rep;
import com.example.demo.requets.createArticleRequest;
import com.example.demo.requets.createUserRequest;
import com.example.demo.services.Neo4j.Users_node_service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;

@Service
public class Users_service {

    private final Users_repository Ur;
    private final Users_node_rep UNr;
    private final Users_node_service UNs;
    private final PasswordEncoder passwordEncoder;
    private final Articles_repository Ar;
    private final Articles_node_rep AR;

    public Users_service(Users_repository ur, Users_node_rep UNR, PasswordEncoder pe,
    Articles_repository ar, Users_node_service uns, Articles_node_rep articleNode) {
        this.Ur = ur;
        this.UNr = UNR;
        this.passwordEncoder=pe;
        this.Ar = ar;
        this.UNs = uns;
        this.AR = articleNode;
    }
    
    public Page<Users> getAllUsers(PageRequest page){
        return Ur.findAll(page);
    }
    
    public Users getUserByUsername(String username) {
        Optional<Users> user = Ur.findByUsername(username);
        if(user.isPresent()){
            return user.get();
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username: " + username);
        }
    }
    @Transactional
    public Users createUser(createUserRequest request) {
        Optional<Users> optionalUser = Ur.findByUsername(request.getUsername());
        if(!optionalUser.isPresent()){
            Users userNew = new Users();
            UsersNode userNode = new UsersNode();
            Date signUpDate = new Date();
            
            userNew.setUsername(request.getUsername());
            userNew.setRoles(request.getRoles());
            userNew.setSignup_date(signUpDate.toString());
            // Hash the password using BCrypt
            String hashedPassword = passwordEncoder.encode(request.getPassword());
            userNew.setPassword(hashedPassword);
            Ur.save(userNew);

            userNode.setMongoId(userNew.get_id());
            userNode.setUserName(userNew.getUsername());
            UNr.save(userNode);
            return userNew;
        }
        else{
            throw new RuntimeException("Username already taken");
        }
        
    }
    @Transactional
    public Users updateUser(String id, Users updatedUser) {
       Optional<Users> optionalUser = Ur.findById(id);
        if (optionalUser.isPresent()) {
            Users existingUser = optionalUser.get();
            Optional<UsersNode> optionalUserNode = UNr.findByMongoId(existingUser.get_id());
            if(optionalUserNode.isPresent()){
                UsersNode existingUserNode = optionalUserNode.get();
                existingUser.setUsername(updatedUser.getUsername());
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword())); // encodeing again if password changes
                existingUser.setRoles(updatedUser.getRoles());
                
                existingUserNode.setUserName(updatedUser.getUsername());
                UNr.save(existingUserNode);
                return Ur.save(existingUser);
            }
            else{
                throw new RuntimeException("User with id: " + id + "not correctly mapped in Neo4j");
            }
            
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }
    @Transactional
    public void deleteUser(String id) {
        Optional<Users> user = Ur.findById(id);
        Optional<UsersNode> userNode = UNr.findByMongoId(id);
        if(user.isPresent()){
            Ur.deleteById(id);
        }
        if(userNode.isPresent()){
            UsersNode existing = userNode.get();
            UNs.deleteUser(existing.get_id());
        }
        else{
            throw new RuntimeErrorException(null, "User not found with id: " + id);
        }
       
    }
    //ARTICLES OPERATIONS
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Articles> createArticle(String username, createArticleRequest request){
        Optional<Users> optionalUser = Ur.findByUsername(username);
        Optional<UsersNode> optionalUserNode = UNr.findByUserName(username);
        if(optionalUser.isPresent() && optionalUserNode.isPresent()){
            Users existiUsers = optionalUser.get();
            UsersNode existiUsersNode = optionalUserNode.get();
            Articles newArticle = new Articles();
            ArticlesNode articleNode = new ArticlesNode();
            newArticle.setAuthor(existiUsers.getUsername());
            newArticle.setTitle(request.getTitle());
            newArticle.setContent(request.getContent());
            Date publishTime = new Date();
            newArticle.setPublish_time(publishTime.toString());
            Articles savedArticle = Ar.save(newArticle);
            articleNode.setMongoId(savedArticle.get_id());
            articleNode.setTitle(savedArticle.getTitle());
            articleNode.setAuthor(existiUsers.getUsername());
            AR.save(articleNode);
            existiUsersNode.getArticlesNodes().add(articleNode);
            UNr.save(existiUsersNode);
            return CompletableFuture.completedFuture(savedArticle);
        }else{
            throw new RuntimeErrorException(null, "User not found with username: " + username);
        }
    }
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Articles> modifyArticle(String username, String articleId, createArticleRequest request){
        Optional<Users> optionalUser = Ur.findByUsername(username);
        Optional<Articles> optionalArticle = Ar.findById(articleId);
        Optional<ArticlesNode> optionalArticleNode = AR.findByMongoId(articleId);
        if(optionalUser.isPresent() && optionalArticle.isPresent() && optionalArticleNode.isPresent()){
            Articles existingArticle = optionalArticle.get();
            Users existingUser = optionalUser.get();
            ArticlesNode existingArticleNode = optionalArticleNode.get();
            if(existingArticle.getAuthor().equals(existingUser.getUsername()) && existingArticleNode.getAuthor().equals(existingUser.getUsername())){
                existingArticle.setContent(request.getContent());
                existingArticle.setTitle(request.getTitle());
                existingArticleNode.setTitle(request.getTitle());
                AR.save(existingArticleNode);
                Date newPublishTime = new Date();
                existingArticle.setPublish_time(newPublishTime.toString());
                return CompletableFuture.completedFuture(Ar.save(existingArticle));
            }
            else{
                throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
            }
        }
        else{
            throw new RuntimeErrorException(null, "User not found with username: " + username+" or Article not present with id:" + articleId);
        }
        
    }
    
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> deleteArticle(String username, String articleId){
        Optional<Users> optionalUser = Ur.findByUsername(username);
        Optional<Articles> optionalArticle = Ar.findById(articleId);
        Optional<ArticlesNode> optionalArticleNode = AR.findByMongoId(articleId);
        Optional<UsersNode> optionalUserNode = UNr.findByUserName(username);
        if(optionalUser.isPresent() && optionalArticle.isPresent() && optionalArticleNode.isPresent() && optionalUserNode.isPresent()){
            Articles existingArticle = optionalArticle.get();
            Users existingUser = optionalUser.get();
            ArticlesNode existingArticleNode = optionalArticleNode.get();
            UsersNode existingUserNode = optionalUserNode.get();
            if(existingArticle.getAuthor().equals(existingUser.getUsername()) && existingArticleNode.getAuthor().equals(existingUserNode.getUserName())){
                Ar.deleteById(articleId);
                existingUserNode.getArticlesNodes().remove(existingArticleNode);
                if(existingUserNode.getLikedArticlesNodes().contains(existingArticleNode)){
                    existingUserNode.getLikedArticlesNodes().remove(existingArticleNode);
                }
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
    //OPERATIONS TO MANAGE AUTHENTICATION
    @Transactional
    public Users registerUser(String username, String password) {
        createUserRequest request = new createUserRequest(
            username,
            password,
            List.of(ROLES.ROLE_USER)
        );
        return createUser(request);
    }
    @Async("customAsyncExecutor")
    @Transactional
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<String> changePassword(String username,String oldPassword, String newPassword){
        Optional<Users> optional = Ur.findByUsername(username);
        if(optional.isPresent()){
            Users existing = optional.get();
            if(passwordEncoder.matches(oldPassword, existing.getPassword())){
                existing.setPassword(passwordEncoder.encode(newPassword));
                Ur.save(existing);
                return CompletableFuture.completedFuture("Password Changed Correctly!");
            }
            else{
                throw new RuntimeErrorException(null, "Wrong Password, try again!");
            }
        }
        else{
            throw new RuntimeErrorException(null, "User not found in MongoDB");
        }
    }
    
    
}
